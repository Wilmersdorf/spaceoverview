package resource

import com.google.inject.Inject
import dao.*
import io.dropwizard.auth.Auth
import mapper.ToDataMapper
import mapper.ToDtoMapper
import model.User
import model.database.LinkData
import model.database.SpaceData
import model.enums.Field
import model.enums.FieldLink
import model.rest.ErrorDto
import model.rest.LinkPropertyDto
import model.rest.LinkSpacePropertyDto
import model.rest.post.PostLinkDto
import model.rest.post.PostSpaceDto
import service.ComputationService
import service.FieldService
import service.ValidationService
import java.time.LocalDateTime
import java.util.*
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import kotlin.collections.HashMap

@Path("/space")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class SpaceResource @Inject constructor(
    private val spaceDao: SpaceDao,
    private val propertyDao: PropertyDao,
    private val linkDao: LinkDao,
    private val referenceDao: ReferenceDao,
    private val toDtoMapper: ToDtoMapper,
    private val toDataMapper: ToDataMapper,
    private val validationService: ValidationService,
    private val computationDao: ComputationDao,
    private val fieldService: FieldService,
    private val computationService: ComputationService
) {

    private val realSet = setOf(FieldLink.REAL, FieldLink.NOT_REAL)
    private val complexSet = setOf(FieldLink.COMPLEX, FieldLink.NOT_COMPLEX)

    private val allowedFieldLinks = mapOf(
        Pair(Field.REAL, Field.REAL) to realSet,
        Pair(Field.REAL, Field.COMPLEX) to emptySet(),
        Pair(Field.REAL, Field.REAL_OR_COMPLEX) to realSet,
        Pair(Field.COMPLEX, Field.REAL) to emptySet(),
        Pair(Field.COMPLEX, Field.COMPLEX) to complexSet,
        Pair(Field.COMPLEX, Field.REAL_OR_COMPLEX) to complexSet,
        Pair(Field.REAL_OR_COMPLEX, Field.REAL) to realSet,
        Pair(Field.REAL_OR_COMPLEX, Field.COMPLEX) to complexSet,
        Pair(Field.REAL_OR_COMPLEX, Field.REAL_OR_COMPLEX) to FieldLink.values().toSet()
    )

    @GET
    fun get(): Response {
        val spaces = spaceDao.getAll().sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.symbol })
        return Response.ok(spaces).build()
    }

    @Path("/{id}")
    @GET
    fun getSpace(@PathParam("id") id: UUID): Response {
        val space = spaceDao.get(id)
        return if (space != null) {
            val references = referenceDao.getBySpaceId(id)
            val spaceDto = toDtoMapper.toSpaceDto(space, references)
            return Response.ok(spaceDto).build()
        } else {
            Response.status(Response.Status.NOT_FOUND).build()
        }
    }

    @POST
    fun add(postSpaceDto: PostSpaceDto, @Auth user: User): Response {
        val errors = validate(postSpaceDto)
        return if (errors.isNotEmpty()) {
            Response.status(Response.Status.BAD_REQUEST).entity(ErrorDto(errors)).build()
        } else {
            val now = LocalDateTime.now()
            val space = SpaceData(
                id = UUID.randomUUID(),
                symbol = postSpaceDto.symbol!!.trim(),
                norm = postSpaceDto.norm!!.trim(),
                description = postSpaceDto.description!!.trim(),
                field = postSpaceDto.field,
                created = now,
                updated = now
            )
            val references = toDataMapper.toSpaceReferences(space.id, now, postSpaceDto.references)
            try {
                spaceDao.create(space)
            } catch (exception: Exception) {
                val duplicateError = mapOf("symbol" to "A space with this symbol already exists.")
                return Response.status(Response.Status.BAD_REQUEST).entity(ErrorDto(duplicateError)).build()
            }
            references.forEach(referenceDao::create)
            val spaceDto = toDtoMapper.toSpaceDto(space, references)
            computationService.compute()
            Response.ok(spaceDto).build()
        }
    }

    @Path("/{id}")
    @POST
    fun update(
        @PathParam("id") id: UUID,
        postSpaceDto: PostSpaceDto,
        @Auth user: User
    ): Response {
        val space = spaceDao.get(id)
        val errors = validate(postSpaceDto)
        return if (space == null) {
            return Response.status(Response.Status.NOT_FOUND).build()
        } else if (errors.isNotEmpty()) {
            Response.status(Response.Status.BAD_REQUEST).entity(ErrorDto(errors)).build()
        } else {
            val now = LocalDateTime.now()
            val spaceUpdate = space.copy(
                symbol = postSpaceDto.symbol!!.trim(),
                norm = postSpaceDto.norm!!.trim(),
                description = postSpaceDto.description!!.trim(),
                field = postSpaceDto.field,
                updated = now
            )
            val references = toDataMapper.toSpaceReferences(id, now, postSpaceDto.references)
            try {
                spaceDao.update(spaceUpdate)
            } catch (exception: Exception) {
                val duplicateError = mapOf("symbol" to "Another space with this symbol already exists.")
                return Response.status(Response.Status.BAD_REQUEST).entity(ErrorDto(duplicateError)).build()
            }
            referenceDao.deleteBySpaceId(id)
            references.forEach(referenceDao::create)
            val spaceDto = toDtoMapper.toSpaceDto(spaceUpdate, references)
            computationService.compute()
            Response.ok(spaceDto).build()
        }
    }

    @Path("/{id}")
    @DELETE
    fun delete(@PathParam("id") id: UUID, @Auth user: User): Response {
        val space = spaceDao.get(id)
        return if (space == null) {
            return Response.status(Response.Status.NOT_FOUND).build()
        } else {
            val links = linkDao.getBySpaceId(id)
            if (links.isNotEmpty()) {
                val errors =
                    mapOf("general" to "Unable to delete space, because it is linked to properties.")
                return Response.status(Response.Status.BAD_REQUEST).entity(ErrorDto(errors)).build()
            } else {
                referenceDao.deleteBySpaceId(id)
                spaceDao.delete(id)
                computationService.compute()
                Response.ok().build()
            }
        }
    }

    @Path("/{spaceId}/property")
    @GET
    fun getProperties(
        @PathParam("spaceId") spaceId: UUID,
        @QueryParam("unlinked") unlinked: Boolean
    ): Response {
        val space = spaceDao.get(spaceId)
        return if (space == null) {
            Response.status(Response.Status.NOT_FOUND).build()
        } else if (!unlinked) {
            val linkedProperties = getLinkedProperties(spaceId)
            Response.ok(linkedProperties).build()
        } else {
            val unlinkedProperties = getUnlinkedProperties(space)
            Response.ok(unlinkedProperties).build()
        }
    }

    private fun getLinkedProperties(spaceId: UUID): List<LinkPropertyDto> {
        val linksByPropertyId = linkDao.getBySpaceId(spaceId).associateBy { it.propertyId }
        val computationsByPropertyId = computationDao.getBySpaceId(spaceId).groupBy { it.propertyId }
        val propertiesIds = linksByPropertyId.keys.union(computationsByPropertyId.keys)
        return propertiesIds.map {
            val link = linksByPropertyId[it]
            val computations = computationsByPropertyId[it].orEmpty()
            val field = fieldService.getCombinedField(link, computations)
            val property = propertyDao.get(it)!!
            val linked = link != null
            val computed = computations.isNotEmpty()
            LinkPropertyDto(field, linked, computed, property)
        }.sortedBy { it.property.name }
    }


    private fun getUnlinkedProperties(space: SpaceData): List<LinkPropertyDto> {
        val linkedPropertyIds = linkDao.getBySpaceId(space.id).map { it.propertyId }.toSet()
        val allowedFields = if (space.field == Field.REAL) {
            setOf(Field.REAL, Field.REAL_OR_COMPLEX)
        } else if (space.field == Field.COMPLEX) {
            setOf(Field.COMPLEX, Field.REAL_OR_COMPLEX)
        } else {
            setOf(Field.REAL, Field.COMPLEX, Field.REAL_OR_COMPLEX)
        }
        return propertyDao.getAll()
            .filter { !linkedPropertyIds.contains(it.id) }
            .filter { allowedFields.contains(it.field) }
            .map { LinkPropertyDto(null, null, null, it) }
            .sortedBy { it.property.name }
    }

    @Path("/{spaceId}/property/{propertyId}")
    @GET
    fun getProperty(
        @PathParam("spaceId") spaceId: UUID,
        @PathParam("propertyId") propertyId: UUID
    ): Response {
        val space = spaceDao.get(spaceId)
        val property = propertyDao.get(propertyId)
        val computations = computationDao.getBySpaceIdAndPropertyId(spaceId, propertyId)
        val link = linkDao.getBySpaceIdAndPropertyId(spaceId, propertyId)
        return if (space == null || property == null || (computations.isEmpty() && link == null)) {
            Response.status(Response.Status.NOT_FOUND).build()
        } else {
            val spaceReferences = referenceDao.getBySpaceId(spaceId)
            val spaceDto = toDtoMapper.toSpaceDto(space, spaceReferences)
            val propertyReferences = referenceDao.getByPropertyId(propertyId)
            val propertyDto = toDtoMapper.toPropertyDto(property, propertyReferences)
            val linkDto = if (link != null) {
                val linkReferences = referenceDao.getByLinkId(link.id)
                toDtoMapper.toLinkDto(link, linkReferences)
            } else {
                null
            }
            val field = fieldService.getCombinedField(link, computations)
            val linkProperty = LinkSpacePropertyDto(spaceDto, field, propertyDto, linkDto, computations)
            Response.ok(linkProperty).build()
        }
    }

    @Path("/{spaceId}/property/{propertyId}")
    @POST
    fun addLink(
        @PathParam("spaceId") spaceId: UUID,
        @PathParam("propertyId") propertyId: UUID,
        postLinkDto: PostLinkDto,
        @Auth user: User
    ): Response {
        val space = spaceDao.get(spaceId)
        val property = propertyDao.get(propertyId)
        if (space == null || property == null) {
            return Response.status(Response.Status.NOT_FOUND).build()
        } else if (postLinkDto.field == null) {
            val error = mapOf("field" to "This field is not allowed.")
            return Response.status(Response.Status.BAD_REQUEST).entity(ErrorDto(error)).build()
        } else {
            val errors = validationService.validateReferences(postLinkDto.references)
            if (errors.isNotEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST).entity(ErrorDto(errors)).build()
            } else {
                val allowedFieldLinks = allowedFieldLinks[Pair(space.field, property.field)].orEmpty()
                if (!allowedFieldLinks.contains(postLinkDto.field)) {
                    val error = mapOf("field" to "This field is not allowed.")
                    return Response.status(Response.Status.BAD_REQUEST).entity(ErrorDto(error)).build()
                } else {
                    val now = LocalDateTime.now()
                    val existingLink = linkDao.getBySpaceIdAndPropertyId(spaceId, propertyId)
                    val link =
                        if (existingLink == null) {
                            val newLink = LinkData(
                                id = UUID.randomUUID(),
                                spaceId = spaceId,
                                propertyId = propertyId,
                                field = postLinkDto.field,
                                created = now,
                                updated = now
                            )
                            linkDao.create(newLink)
                            newLink
                        } else {
                            val updatedLink = existingLink.copy(
                                field = postLinkDto.field,
                                updated = now
                            )
                            linkDao.update(updatedLink)
                            updatedLink
                        }
                    val references = toDataMapper.toLinkReferences(link.id, now, postLinkDto.references)
                    referenceDao.deleteByLinkId(link.id)
                    references.forEach(referenceDao::create)
                    val linkDto = toDtoMapper.toLinkDto(link, references)
                    computationService.compute()
                    return Response.ok(linkDto).build()
                }
            }
        }
    }

    @Path("/{spaceId}/property/{propertyId}")
    @DELETE
    fun deleteLink(
        @PathParam("spaceId") spaceId: UUID,
        @PathParam("propertyId") propertyId: UUID,
        @Auth user: User
    ): Response {
        val link = linkDao.getBySpaceIdAndPropertyId(spaceId, propertyId)
        return if (link == null) {
            Response.status(Response.Status.NOT_FOUND).build()
        } else {
            referenceDao.deleteByLinkId(link.id)
            linkDao.delete(link.id)
            computationService.compute()
            Response.ok().build()
        }
    }

    private fun validate(postSpaceDto: PostSpaceDto): Map<String, String> {
        val errors = HashMap<String, String>()
        errors.putAll(validationService.validate(postSpaceDto.description, "description", 1024))
        errors.putAll(validationService.validate(postSpaceDto.symbol, "symbol", 128))
        errors.putAll(validationService.validate(postSpaceDto.norm, "norm", 128))
        errors.putAll(validationService.validateReferences(postSpaceDto.references))
        return errors
    }

}