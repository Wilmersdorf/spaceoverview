package resource

import com.google.inject.Inject
import dao.LinkDao
import dao.PropertyDao
import dao.ReferenceDao
import dao.SpaceDao
import io.dropwizard.auth.Auth
import mapper.ToDataMapper
import mapper.ToDtoMapper
import model.User
import model.database.PropertyData
import model.rest.ErrorDto
import model.rest.LinkSpaceDto
import model.rest.PostPropertyDto
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
    private val validationService: ValidationService
) {

    @GET
    fun get(): Response {
        val properties = propertyDao.getAll().sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER, { it.name }))
        return Response.ok(properties).build()
    }

    @Path("/{id}")
    @GET
    fun getProperty(@PathParam("id") id: UUID): Response {
        val property = propertyDao.get(id)
        return if (property != null) {
            val references = referenceDao.getByPropertyId(id)
            val propertyDto = toDtoMapper.toPropertyDto(property, references)
            return Response.ok(propertyDto).build()
        } else {
            Response.status(Response.Status.NOT_FOUND).build()
        }
    }

    @POST
    fun add(postPropertyDto: PostPropertyDto, @Auth user: User): Response {
        val now = LocalDateTime.now()
        val errors = validate(postPropertyDto)
        return if (errors.isNotEmpty()) {
            Response.status(Response.Status.BAD_REQUEST).entity(ErrorDto(errors)).build()
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
                val duplicateError = mapOf("name" to "A property with this name already exists.")
                return Response.status(Response.Status.BAD_REQUEST).entity(ErrorDto(duplicateError)).build()
            }
            references.forEach(referenceDao::create)
            val propertyDto = toDtoMapper.toPropertyDto(property, references)
            Response.ok(propertyDto).build()
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
        return if (property == null) {
            return Response.status(Response.Status.NOT_FOUND).build()
        } else if (errors.isNotEmpty()) {
            Response.status(Response.Status.BAD_REQUEST).entity(ErrorDto(errors)).build()
        } else {
            val now = LocalDateTime.now()
            val propertyUpdate = property.copy(
                name = postPropertyDto.name!!.trim(),
                description = postPropertyDto.description!!.trim(),
                field = postPropertyDto.field,
                updated = LocalDateTime.now()
            )
            val references = toDataMapper.toPropertyReferences(id, now, postPropertyDto.references)
            try {
                propertyDao.update(propertyUpdate)
            } catch (exception: Exception) {
                val duplicateError = mapOf("name" to "Another property with this name already exists.")
                return Response.status(Response.Status.BAD_REQUEST).entity(ErrorDto(duplicateError)).build()
            }
            referenceDao.deleteByPropertyId(id)
            references.forEach(referenceDao::create)
            val propertyDto = toDtoMapper.toPropertyDto(propertyUpdate, references)
            Response.ok(propertyDto).build()
        }
    }

    @Path("/{id}")
    @DELETE
    fun delete(@PathParam("id") id: UUID, @Auth user: User): Response {
        val property = propertyDao.get(id)
        return if (property == null) {
            return Response.status(Response.Status.NOT_FOUND).build()
        } else {
            val links = linkDao.getByPropertyId(id)
            if (links.isNotEmpty()) {
                val errors =
                    mapOf("general" to "Unable to delete property, because it is linked to spaces.")
                return Response.status(Response.Status.BAD_REQUEST).entity(ErrorDto(errors)).build()
            } else {
                referenceDao.deleteByPropertyId(id)
                propertyDao.delete(id)
                Response.ok().build()
            }
        }
    }

    @Path("/{id}/space")
    @GET
    fun getSpaces(@PathParam("id") id: UUID): Response {
        val property = propertyDao.get(id)
        return if (property == null) {
            Response.status(Response.Status.NOT_FOUND).build()
        } else {
            val linkSpaceList = linkDao.getByPropertyId(id).map {
                val space = spaceDao.get(it.spaceId)!!
                LinkSpaceDto(it, space)
            }
            Response.ok(linkSpaceList).build()
        }
    }

    private fun validate(postPropertyDto: PostPropertyDto): HashMap<String, String> {
        val errors = HashMap<String, String>()
        errors.putAll(validationService.validate(postPropertyDto.description, "description", 1024))
        errors.putAll(validationService.validate(postPropertyDto.name, "name", 128))
        errors.putAll(validationService.validateReferences(postPropertyDto.references))
        return errors
    }

}