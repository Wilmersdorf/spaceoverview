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
import model.database.DifferentialEquationData
import model.database.DifferentialEquationLinkData
import model.rest.DifferentialEquationLinkPropertyDto
import model.rest.post.PostDifferentialEquationDto
import model.rest.post.PostDifferentialEquationLinkDto
import service.ValidationService
import util.reduceToNull
import java.time.ZonedDateTime
import java.util.*

@Path("/differential-equation")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class DifferentialEquationResource @Inject constructor(
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
        val differentialEquations = differentialEquationDao.getAll()
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
        return Response.ok(differentialEquations).build()
    }

    @Path("/{id}")
    @GET
    fun getDifferentialEquation(@PathParam("id") id: UUID): Response {
        val differentialEquation = differentialEquationDao.get(id)
        if (differentialEquation == null) {
            throw NotFoundException()
        } else {
            val references = referenceDao.getByDifferentialEquationId(id)
            val differentialEquationDto = toDtoMapper.toDifferentialEquationDto(differentialEquation, references)
            return Response.ok(differentialEquationDto).build()
        }
    }

    @POST
    fun add(@Auth user: User, postDto: PostDifferentialEquationDto): Response {
        val errors = validate(postDto)
        if (errors.isNotEmpty()) {
            throw JsonException(errors)
        } else {
            val now = ZonedDateTime.now()
            val differentialEquation = DifferentialEquationData(
                id = UUID.randomUUID(),
                name = postDto.name!!.trim(),
                symbol = postDto.symbol!!.trim(),
                description = postDto.description!!.trim(),
                variables = postDto.variables!!.trim(),
                parameters = postDto.parameters!!.trim(),
                created = now,
                updated = now
            )
            val references =
                toDataMapper.toDifferentialEquationReferences(differentialEquation.id, now, postDto.references)
            try {
                differentialEquationDao.create(differentialEquation)
            } catch (exception: Exception) {
                throw JsonException(
                    key = "name",
                    value = "A differential equation with this name already exists."
                )
            }
            references.forEach(referenceDao::create)
            val differentialEquationDto = toDtoMapper.toDifferentialEquationDto(differentialEquation, references)
            return Response.ok(differentialEquationDto).build()
        }
    }

    @Path("/{id}")
    @POST
    fun update(@Auth user: User, @PathParam("id") id: UUID, postDto: PostDifferentialEquationDto): Response {
        val differentialEquation = differentialEquationDao.get(id)
        val errors = validate(postDto)
        if (differentialEquation == null) {
            throw NotFoundException()
        } else if (errors.isNotEmpty()) {
            throw JsonException(errors)
        } else {
            val now = ZonedDateTime.now()
            val update = differentialEquation.copy(
                name = postDto.name!!.trim(),
                symbol = postDto.symbol!!.trim(),
                description = postDto.description!!.trim(),
                variables = postDto.variables!!.trim(),
                parameters = postDto.parameters!!.trim(),
                updated = now
            )
            val references =
                toDataMapper.toDifferentialEquationReferences(differentialEquation.id, now, postDto.references)
            try {
                differentialEquationDao.update(update)
            } catch (exception: Exception) {
                throw JsonException(
                    key = "name",
                    value = "Another differential equation with this name already exists."
                )
            }
            referenceDao.deleteByDifferentialEquationId(id)
            references.forEach(referenceDao::create)
            val differentialEquationDto = toDtoMapper.toDifferentialEquationDto(update, references)
            return Response.ok(differentialEquationDto).build()
        }
    }

    @Path("/{id}")
    @DELETE
    fun delete(@Auth user: User, @PathParam("id") id: UUID): Response {
        val differentialEquation = differentialEquationDao.get(id)
        if (differentialEquation == null) {
            throw NotFoundException()
        } else {
            val links = differentialEquationLinkDao.getByDifferentialEquationId(id)
            if (links.isNotEmpty()) {
                throw JsonException(
                    key = "general",
                    value = "Unable to delete differential equation, because it is linked to properties."
                )
            } else {
                referenceDao.deleteByDifferentialEquationId(id)
                differentialEquationDao.delete(id)
                return Response.ok().build()
            }
        }
    }

    @Path("/{id}/property")
    @GET
    fun getProperties(
        @PathParam("id") id: UUID,
        @QueryParam("unlinked") unlinked: Boolean
    ): Response {
        val differentialEquation = differentialEquationDao.get(id)
        return if (differentialEquation == null) {
            throw NotFoundException()
        } else if (!unlinked) {
            val linkedProperties = getLinkedProperties(id)
            Response.ok(linkedProperties).build()
        } else {
            val unlinkedProperties = getUnlinkedProperties(id)
            Response.ok(unlinkedProperties).build()
        }
    }

    @Path("/{id}/property/{propertyId}")
    @GET
    fun getLink(@PathParam("id") id: UUID, @PathParam("propertyId") propertyId: UUID): Response {
        val link = differentialEquationLinkDao.getByDifferentialEquationIdAndDifferentialEquationPropertyId(
            id,
            propertyId
        )
        if (link == null) {
            throw NotFoundException()
        } else {
            val linkReferences = referenceDao.getByDifferentialEquationLinkId(link.id)
            val linkDto = toDtoMapper.toDifferentialEquationLinkDto(link, linkReferences)
            return Response.ok(linkDto).build()
        }
    }

    @Path("/{id}/property/{propertyId}")
    @POST
    fun addLink(
        @Auth user: User,
        @PathParam("id") id: UUID,
        @PathParam("propertyId") propertyId: UUID,
        postDto: PostDifferentialEquationLinkDto,
    ): Response {
        val differentialEquation = differentialEquationDao.get(id)
        val property = differentialEquationPropertyDao.get(propertyId)
        if (differentialEquation == null || property == null) {
            throw NotFoundException()
        } else {
            val errors = validate(postDto)
            if (errors.isNotEmpty()) {
                throw JsonException(errors)
            } else {
                val now = ZonedDateTime.now()
                val existingLink =
                    differentialEquationLinkDao.getByDifferentialEquationIdAndDifferentialEquationPropertyId(
                        id,
                        propertyId
                    )
                val link = if (existingLink == null) {
                    val newLink = DifferentialEquationLinkData(
                        id = UUID.randomUUID(),
                        differentialEquationId = id,
                        differentialEquationPropertyId = propertyId,
                        hasProperty = postDto.hasProperty!!,
                        description = postDto.description.reduceToNull(),
                        created = now,
                        updated = now
                    )
                    differentialEquationLinkDao.create(newLink)
                    newLink
                } else {
                    val updatedLink = existingLink.copy(
                        hasProperty = postDto.hasProperty!!,
                        description = postDto.description.reduceToNull(),
                        updated = now
                    )
                    differentialEquationLinkDao.update(updatedLink)
                    updatedLink
                }
                val references = toDataMapper.toDifferentialEquationLinkReferences(
                    differentialEquationLinkId = link.id,
                    time = now,
                    references = postDto.references
                )
                referenceDao.deleteByDifferentialEquationLinkId(link.id)
                references.forEach(referenceDao::create)
                val linkDto = toDtoMapper.toDifferentialEquationLinkDto(link, references)
                return Response.ok(linkDto).build()
            }
        }
    }

    @Path("/{id}/property/{propertyId}")
    @DELETE
    fun deleteLink(@Auth user: User, @PathParam("id") id: UUID, @PathParam("propertyId") propertyId: UUID): Response {
        val link = differentialEquationLinkDao.getByDifferentialEquationIdAndDifferentialEquationPropertyId(
            id,
            propertyId
        )
        if (link == null) {
            throw NotFoundException()
        } else {
            referenceDao.deleteByDifferentialEquationLinkId(link.id)
            differentialEquationLinkDao.delete(link.id)
            return Response.ok().build()
        }
    }

    private fun getLinkedProperties(id: UUID): List<DifferentialEquationLinkPropertyDto> {
        return differentialEquationLinkDao.getByDifferentialEquationId(id).map {
            val link = it
            val property = differentialEquationPropertyDao.get(it.differentialEquationPropertyId)!!
            DifferentialEquationLinkPropertyDto(
                hasProperty = link.hasProperty,
                differentialEquationProperty = property
            )
        }.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.differentialEquationProperty.name })
    }

    private fun getUnlinkedProperties(id: UUID): List<DifferentialEquationLinkPropertyDto> {
        val linkedPropertyIds = differentialEquationLinkDao.getByDifferentialEquationId(id)
            .map { it.differentialEquationPropertyId }
            .toSet()
        return differentialEquationPropertyDao.getAll()
            .filter { !linkedPropertyIds.contains(it.id) }
            .map {
                DifferentialEquationLinkPropertyDto(
                    hasProperty = null,
                    differentialEquationProperty = it
                )
            }.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.differentialEquationProperty.name })
    }

    private fun validate(postDto: PostDifferentialEquationDto): Map<String, String> {
        val errors = HashMap<String, String>()
        errors.putAll(validationService.validate(postDto.name, "name", 128))
        errors.putAll(validationService.validate(postDto.symbol, "symbol", 128))
        errors.putAll(validationService.validate(postDto.description, "description", 1024))
        errors.putAll(validationService.validate(postDto.variables, "variables", 128))
        errors.putAll(validationService.validate(postDto.parameters, "parameters", 128))
        errors.putAll(validationService.validateReferences(postDto.references))
        return errors
    }

    private fun validate(postDifferentialEquationLinkDto: PostDifferentialEquationLinkDto): Map<String, String> {
        val errors = HashMap<String, String>()
        if (postDifferentialEquationLinkDto.hasProperty == null) {
            errors["hasProperty"] = "Please select if the differential equation has or doesn't have this property."
        }
        errors.putAll(
            validationService.validateIfNotBlank(
                postDifferentialEquationLinkDto.description,
                "description",
                1024
            )
        )
        errors.putAll(validationService.validateReferences(postDifferentialEquationLinkDto.references))
        return errors
    }

}
