package resource

import com.google.inject.Inject
import dao.*
import exception.JsonException
import io.dropwizard.auth.Auth
import mapper.ToDataMapper
import mapper.ToDtoMapper
import model.User
import model.database.PropertyData
import model.rest.LinkSpaceDto
import model.rest.post.PostPropertyDto
import service.ComputationService
import service.FieldService
import service.ValidationService
import java.time.LocalDateTime
import java.util.*
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import kotlin.collections.HashMap

@Path("/property")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class PropertyResource @Inject constructor(
    private val propertyDao: PropertyDao,
    private val linkDao: LinkDao,
    private val spaceDao: SpaceDao,
    private val referenceDao: ReferenceDao,
    private val toDtoMapper: ToDtoMapper,
    private val toDataMapper: ToDataMapper,
    private val validationService: ValidationService,
    private val conditionDao: ConditionDao,
    private val conclusionDao: ConclusionDao,
    private val computationDao: ComputationDao,
    private val fieldService: FieldService,
    private val computationService: ComputationService
) {

    @GET
    fun get(): Response {
        val properties = propertyDao.getAll().sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
        return Response.ok(properties).build()
    }

    @Path("/{id}")
    @GET
    fun getProperty(@PathParam("id") id: UUID): Response {
        val property = propertyDao.get(id)
        if (property == null) {
            throw NotFoundException()
        } else {
            val references = referenceDao.getByPropertyId(id)
            val propertyDto = toDtoMapper.toPropertyDto(property, references)
            return Response.ok(propertyDto).build()
        }
    }

    @POST
    fun add(postPropertyDto: PostPropertyDto, @Auth user: User): Response {
        val now = LocalDateTime.now()
        val errors = validate(postPropertyDto)
        if (errors.isNotEmpty()) {
            throw JsonException(errors)
        } else {
            val property = PropertyData(
                id = UUID.randomUUID(),
                name = postPropertyDto.name!!.trim(),
                description = postPropertyDto.description!!.trim(),
                field = postPropertyDto.field,
                created = now,
                updated = now
            )
            val references = toDataMapper.toPropertyReferences(property.id, now, postPropertyDto.references)
            try {
                propertyDao.create(property)
            } catch (exception: Exception) {
                throw JsonException(
                    key = "name",
                    value = "A property with this name already exists."
                )
            }
            references.forEach(referenceDao::create)
            val propertyDto = toDtoMapper.toPropertyDto(property, references)
            computationService.compute()
            return Response.ok(propertyDto).build()
        }
    }

    @Path("/{id}")
    @POST
    fun update(
        @PathParam("id") id: UUID,
        postPropertyDto: PostPropertyDto,
        @Auth user: User
    ): Response {
        val property = propertyDao.get(id)
        val errors = validate(postPropertyDto)
        if (property == null) {
            throw NotFoundException()
        } else if (errors.isNotEmpty()) {
            throw JsonException(errors)
        } else {
            val now = LocalDateTime.now()
            val propertyUpdate = property.copy(
                name = postPropertyDto.name!!.trim(),
                description = postPropertyDto.description!!.trim(),
                field = postPropertyDto.field,
                updated = now
            )
            val references = toDataMapper.toPropertyReferences(id, now, postPropertyDto.references)
            try {
                propertyDao.update(propertyUpdate)
            } catch (exception: Exception) {
                throw JsonException(
                    key = "name",
                    value = "Another property with this name already exists."
                )
            }
            referenceDao.deleteByPropertyId(id)
            references.forEach(referenceDao::create)
            val propertyDto = toDtoMapper.toPropertyDto(propertyUpdate, references)
            computationService.compute()
            return Response.ok(propertyDto).build()
        }
    }

    @Path("/{id}")
    @DELETE
    fun delete(@PathParam("id") id: UUID, @Auth user: User): Response {
        val property = propertyDao.get(id)
        if (property == null) {
            throw NotFoundException()
        } else {
            val links = linkDao.getByPropertyId(id)
            if (links.isNotEmpty()) {
                throw JsonException(
                    key = "general",
                    value = "Unable to delete property, because it is linked to spaces."
                )
            } else {
                val conditions = conditionDao.getByPropertyId(id)
                val conclusions = conclusionDao.getByPropertyId(id)
                if (conditions.isNotEmpty() || conclusions.isNotEmpty()) {
                    throw JsonException(
                        key = "general",
                        value = "Unable to delete property, because it is used in theorems."
                    )
                } else {
                    referenceDao.deleteByPropertyId(id)
                    propertyDao.delete(id)
                    computationService.compute()
                    return Response.ok().build()
                }
            }
        }
    }

    @Path("/{id}/space")
    @GET
    fun getSpaces(@PathParam("id") id: UUID): Response {
        val property = propertyDao.get(id)
        if (property == null) {
            throw NotFoundException()
        } else {
            val linksBySpaceId = linkDao.getByPropertyId(id).associateBy { it.spaceId }
            val computationsBySpaceId = computationDao.getByPropertyId(id).groupBy { it.spaceId }
            val spaceIds = linksBySpaceId.keys.union(computationsBySpaceId.keys)
            val linkedSpaces = spaceIds.map {
                val link = linksBySpaceId[it]
                val computations = computationsBySpaceId[it].orEmpty()
                val field = fieldService.getCombinedField(link, computations)
                val space = spaceDao.get(it)!!
                val linked = link != null
                val computed = computations.isNotEmpty()
                LinkSpaceDto(field, linked, computed, space)
            }.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.space.symbol })
            return Response.ok(linkedSpaces).build()
        }
    }

    private fun validate(postPropertyDto: PostPropertyDto): Map<String, String> {
        val errors = HashMap<String, String>()
        errors.putAll(validationService.validate(postPropertyDto.description, "description", 1024))
        errors.putAll(validationService.validate(postPropertyDto.name, "name", 128))
        errors.putAll(validationService.validateReferences(postPropertyDto.references))
        return errors
    }

}
