package resource

import com.google.inject.Inject
import dao.DifferentialEquationDao
import dao.DifferentialEquationLinkDao
import dao.DifferentialEquationPropertyDao
import dao.ReferenceDao
import exception.JsonException
import io.dropwizard.auth.Auth
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import mapper.ToDataMapper
import mapper.ToDtoMapper
import model.User
import model.database.DifferentialEquationPropertyData
import model.rest.DifferentialEquationLinkDifferentialEquationDto
import model.rest.post.PostDifferentialEquationPropertyDto
import service.ValidationService
import java.time.ZonedDateTime
import java.util.*

@Path("/differential-equation/property")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class DifferentialEquationPropertyResource @Inject constructor(
    private val differentialEquationDao: DifferentialEquationDao,
    private val differentialEquationLinkDao: DifferentialEquationLinkDao,
    private val differentialEquationPropertyDao: DifferentialEquationPropertyDao,
    private val referenceDao: ReferenceDao,
    private val toDataMapper: ToDataMapper,
    private val toDtoMapper: ToDtoMapper,
    private val validationService: ValidationService
) {

    @GET
    fun get(): Response {
        val differentialEquationProperties = differentialEquationPropertyDao.getAll()
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
        return Response.ok(differentialEquationProperties).build()
    }

    @Path("/{id}")
    @GET
    fun getDifferentialEquationProperty(@PathParam("id") id: UUID): Response {
        val differentialEquationProperty = differentialEquationPropertyDao.get(id)
        if (differentialEquationProperty == null) {
            throw NotFoundException()
        } else {
            val references = referenceDao.getByDifferentialEquationPropertyId(id)
            val differentialEquationPropertyDto =
                toDtoMapper.toDifferentialEquationPropertyDto(differentialEquationProperty, references)
            return Response.ok(differentialEquationPropertyDto).build()
        }
    }

    @POST
    fun add(@Auth user: User, postDto: PostDifferentialEquationPropertyDto): Response {
        val errors = validate(postDto)
        if (errors.isNotEmpty()) {
            throw JsonException(errors)
        } else {
            val now = ZonedDateTime.now()
            val differentialEquationProperty = DifferentialEquationPropertyData(
                id = UUID.randomUUID(),
                name = postDto.name!!.trim(),
                description = postDto.description!!.trim(),
                created = now,
                updated = now
            )
            val references = toDataMapper.toDifferentialEquationPropertyReferences(
                differentialEquationProperty.id,
                now,
                postDto.references
            )
            try {
                differentialEquationPropertyDao.create(differentialEquationProperty)
            } catch (exception: Exception) {
                throw JsonException(
                    key = "name",
                    value = "A differential equation property with this name already exists."
                )
            }
            references.forEach(referenceDao::create)
            val differentialEquationDto =
                toDtoMapper.toDifferentialEquationPropertyDto(differentialEquationProperty, references)
            return Response.ok(differentialEquationDto).build()
        }
    }

    @Path("/{id}")
    @POST
    fun update(@Auth user: User, @PathParam("id") id: UUID, postDto: PostDifferentialEquationPropertyDto): Response {
        val differentialEquationProperty = differentialEquationPropertyDao.get(id)
        val errors = validate(postDto)
        if (differentialEquationProperty == null) {
            throw NotFoundException()
        } else if (errors.isNotEmpty()) {
            throw JsonException(errors)
        } else {
            val now = ZonedDateTime.now()
            val update = differentialEquationProperty.copy(
                name = postDto.name!!.trim(),
                description = postDto.description!!.trim(),
                updated = now
            )
            val references = toDataMapper.toDifferentialEquationPropertyReferences(
                differentialEquationProperty.id,
                now,
                postDto.references
            )
            try {
                differentialEquationPropertyDao.update(update)
            } catch (exception: Exception) {
                throw JsonException(
                    key = "name",
                    value = "Another differential equation property with this name already exists."
                )
            }
            referenceDao.deleteByDifferentialEquationPropertyId(id)
            references.forEach(referenceDao::create)
            val differentialEquationPropertyDto =
                toDtoMapper.toDifferentialEquationPropertyDto(update, references)
            return Response.ok(differentialEquationPropertyDto).build()
        }
    }

    @Path("/{id}")
    @DELETE
    fun delete(@Auth user: User, @PathParam("id") id: UUID): Response {
        val differentialEquationProperty = differentialEquationPropertyDao.get(id)
        if (differentialEquationProperty == null) {
            throw NotFoundException()
        } else {
            val links = differentialEquationLinkDao.getByDifferentialEquationPropertyId(id)
            if (links.isNotEmpty()) {
                throw JsonException(
                    key = "general",
                    value = "Unable to delete property, because it is linked to differential equations."
                )
            } else {
                referenceDao.deleteByDifferentialEquationPropertyId(id)
                differentialEquationPropertyDao.delete(id)
                return Response.ok().build()
            }
        }
    }

    @Path("/{id}/differential-equation")
    @GET
    fun getDifferentialEquations(@PathParam("id") id: UUID): Response {
        val differentialEquationProperty = differentialEquationPropertyDao.get(id)
        if (differentialEquationProperty == null) {
            throw NotFoundException()
        } else {
            val linkedDifferentialEquations = differentialEquationLinkDao.getByDifferentialEquationPropertyId(id).map {
                val link = it
                val differentialEquation = differentialEquationDao.get(it.differentialEquationId)!!
                DifferentialEquationLinkDifferentialEquationDto(
                    hasProperty = link.hasProperty,
                    differentialEquation = differentialEquation
                )
            }.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.differentialEquation.name })
            return Response.ok(linkedDifferentialEquations).build()
        }
    }

    private fun validate(postDto: PostDifferentialEquationPropertyDto): Map<String, String> {
        val errors = HashMap<String, String>()
        errors.putAll(validationService.validate(postDto.name, "name", 128))
        errors.putAll(validationService.validate(postDto.description, "description", 1024))
        errors.putAll(validationService.validateReferences(postDto.references))
        return errors
    }

}
