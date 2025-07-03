package resource

import com.google.inject.Inject
import dao.*
import exception.JsonException
import io.dropwizard.auth.Auth
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import mapper.ToDataMapper
import mapper.ToDtoMapper
import model.User
import model.database.LinkData
import model.database.SpaceData
import model.enums.Field
import model.enums.FieldLink
import model.rest.LinkPropertyDto
import model.rest.LinkSpacePropertyDto
import model.rest.post.PostLinkDto
import model.rest.post.PostSpaceDto
import service.ComputationService
import service.FieldService
import service.ValidationService
import util.reduceToNull
import java.time.ZonedDateTime
import java.util.*

@Path("/space")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class SpaceResource @Inject constructor(
    private val computationDao: ComputationDao,
    private val computationService: ComputationService,
    private val fieldService: FieldService,
    private val linkDao: LinkDao,
    private val propertyDao: PropertyDao,
    private val referenceDao: ReferenceDao,
    private val spaceDao: SpaceDao,
    private val toDataMapper: ToDataMapper,
    private val toDtoMapper: ToDtoMapper,
    private val validationService: ValidationService
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
        Pair(Field.REAL_OR_COMPLEX, Field.REAL_OR_COMPLEX) to FieldLink.entries.toSet()
    )

    @GET
    fun get(): Response {
        val spaces = spaceDao.getAll()
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.symbol })
        return Response.ok(spaces).build()
    }

    @Path("/{id}")
    @GET
    fun getSpace(@PathParam("id") id: UUID): Response {
        val space = spaceDao.get(id)
        if (space == null) {
            throw NotFoundException()
        } else {
            val references = referenceDao.getBySpaceId(id)
            val spaceDto = toDtoMapper.toSpaceDto(space, references)
            return Response.ok(spaceDto).build()
        }
    }

    @POST
    fun add(@Auth user: User, postSpaceDto: PostSpaceDto): Response {
        val errors = validate(postSpaceDto)
        if (errors.isNotEmpty()) {
            throw JsonException(errors)
        } else {
            val now = ZonedDateTime.now()
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
                throw JsonException(
                    key = "symbol",
                    value = "A space with this symbol already exists."
                )
            }
            references.forEach(referenceDao::create)
            val spaceDto = toDtoMapper.toSpaceDto(space, references)
            computationService.compute()
            return Response.ok(spaceDto).build()
        }
    }

    @Path("/{id}")
    @POST
    fun update(@Auth user: User, @PathParam("id") id: UUID, postSpaceDto: PostSpaceDto): Response {
        val space = spaceDao.get(id)
        val errors = validate(postSpaceDto)
        if (space == null) {
            throw NotFoundException()
        } else if (errors.isNotEmpty()) {
            throw JsonException(errors)
        } else {
            val now = ZonedDateTime.now()
            val update = space.copy(
                symbol = postSpaceDto.symbol!!.trim(),
                norm = postSpaceDto.norm!!.trim(),
                description = postSpaceDto.description!!.trim(),
                field = postSpaceDto.field,
                updated = now
            )
            val references = toDataMapper.toSpaceReferences(id, now, postSpaceDto.references)
            try {
                spaceDao.update(update)
            } catch (exception: Exception) {
                throw JsonException(
                    key = "symbol",
                    value = "Another space with this symbol already exists."
                )
            }
            referenceDao.deleteBySpaceId(id)
            references.forEach(referenceDao::create)
            val spaceDto = toDtoMapper.toSpaceDto(update, references)
            computationService.compute()
            return Response.ok(spaceDto).build()
        }
    }

    @Path("/{id}")
    @DELETE
    fun delete(@Auth user: User, @PathParam("id") id: UUID): Response {
        val space = spaceDao.get(id)
        if (space == null) {
            throw NotFoundException()
        } else {
            val links = linkDao.getBySpaceId(id)
            if (links.isNotEmpty()) {
                throw JsonException(
                    key = "general",
                    value = "Unable to delete space, because it is linked to properties."
                )
            } else {
                referenceDao.deleteBySpaceId(id)
                spaceDao.delete(id)
                computationService.compute()
                return Response.ok().build()
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
            throw NotFoundException()
        } else if (!unlinked) {
            val linkedProperties = getLinkedProperties(spaceId)
            Response.ok(linkedProperties).build()
        } else {
            val unlinkedProperties = getUnlinkedProperties(space)
            Response.ok(unlinkedProperties).build()
        }
    }

    private fun getLinkedProperties(spaceId: UUID): List<LinkPropertyDto> {
        val propertyIdToLink = linkDao.getBySpaceId(spaceId).associateBy { it.propertyId }
        val propertyIdToComputations = computationDao.getBySpaceId(spaceId).groupBy { it.propertyId }
        val propertyIds = propertyIdToLink.keys.union(propertyIdToComputations.keys)
        return propertyIds.map {
            val link = propertyIdToLink[it]
            val computations = propertyIdToComputations[it].orEmpty()
            val field = fieldService.getCombinedField(link, computations)
            val property = propertyDao.get(it)!!
            val linked = link != null
            val computed = computations.isNotEmpty()
            LinkPropertyDto(field, linked, computed, property)
        }.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.property.name })
    }

    private fun getUnlinkedProperties(space: SpaceData): List<LinkPropertyDto> {
        val linkedPropertyIds = linkDao.getBySpaceId(space.id).map { it.propertyId }.toSet()
        val allowedFields = when (space.field) {
            Field.REAL -> setOf(Field.REAL, Field.REAL_OR_COMPLEX)
            Field.COMPLEX -> setOf(Field.COMPLEX, Field.REAL_OR_COMPLEX)
            else -> setOf(Field.REAL, Field.COMPLEX, Field.REAL_OR_COMPLEX)
        }
        return propertyDao.getAll()
            .filter { !linkedPropertyIds.contains(it.id) }
            .filter { allowedFields.contains(it.field) }
            .map { LinkPropertyDto(null, null, null, it) }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.property.name })
    }

    @Path("/{spaceId}/property/{propertyId}")
    @GET
    fun getLink(
        @PathParam("spaceId") spaceId: UUID,
        @PathParam("propertyId") propertyId: UUID
    ): Response {
        val space = spaceDao.get(spaceId)
        val property = propertyDao.get(propertyId)
        val computations = computationDao.getBySpaceIdAndPropertyId(spaceId, propertyId)
        val link = linkDao.getBySpaceIdAndPropertyId(spaceId, propertyId)
        if (space == null || property == null || (computations.isEmpty() && link == null)) {
            throw NotFoundException()
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
            return Response.ok(linkProperty).build()
        }
    }

    @Path("/{spaceId}/property/{propertyId}")
    @POST
    fun addLink(
        @Auth user: User,
        @PathParam("spaceId") spaceId: UUID,
        @PathParam("propertyId") propertyId: UUID,
        postLinkDto: PostLinkDto,
    ): Response {
        val space = spaceDao.get(spaceId)
        val property = propertyDao.get(propertyId)
        if (space == null || property == null) {
            throw NotFoundException()
        } else {
            val errors = validate(postLinkDto)
            if (errors.isNotEmpty()) {
                throw JsonException(errors)
            } else {
                val allowedFieldLinks = allowedFieldLinks[Pair(space.field, property.field)].orEmpty()
                if (!allowedFieldLinks.contains(postLinkDto.field)) {
                    throw JsonException(
                        key = "field",
                        value = "This field is not allowed."
                    )
                } else {
                    val now = ZonedDateTime.now()
                    val existingLink = linkDao.getBySpaceIdAndPropertyId(spaceId, propertyId)
                    val link = if (existingLink == null) {
                        val newLink = LinkData(
                            id = UUID.randomUUID(),
                            spaceId = spaceId,
                            propertyId = propertyId,
                            field = postLinkDto.field!!,
                            description = postLinkDto.description.reduceToNull(),
                            created = now,
                            updated = now
                        )
                        linkDao.create(newLink)
                        newLink
                    } else {
                        val updatedLink = existingLink.copy(
                            field = postLinkDto.field!!,
                            description = postLinkDto.description.reduceToNull(),
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
        @Auth user: User,
        @PathParam("spaceId") spaceId: UUID,
        @PathParam("propertyId") propertyId: UUID,
    ): Response {
        val link = linkDao.getBySpaceIdAndPropertyId(spaceId, propertyId)
        if (link == null) {
            throw NotFoundException()
        } else {
            referenceDao.deleteByLinkId(link.id)
            linkDao.delete(link.id)
            computationService.compute()
            return Response.ok().build()
        }
    }

    private fun validate(postSpaceDto: PostSpaceDto): Map<String, String> {
        val errors = HashMap<String, String>()
        errors.putAll(validationService.validate(postSpaceDto.description, "description", 1024, true))
        errors.putAll(validationService.validate(postSpaceDto.norm, "norm", 128, true))
        errors.putAll(validationService.validate(postSpaceDto.symbol, "symbol", 128, false))
        errors.putAll(validationService.validateReferences(postSpaceDto.references))
        return errors
    }

    private fun validate(postLinkDto: PostLinkDto): Map<String, String> {
        val errors = HashMap<String, String>()
        if (postLinkDto.field == null) {
            errors["field"] = "This field is not allowed."
        }
        errors.putAll(
            validationService.validateIfNotBlank(
                postLinkDto.description,
                "description",
                1024,
                true
            )
        )
        errors.putAll(validationService.validateReferences(postLinkDto.references))
        return errors
    }

}
