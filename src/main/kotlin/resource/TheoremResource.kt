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
import model.database.TheoremData
import model.enums.Field
import model.enums.FieldLink
import model.rest.post.PostConclusionDto
import model.rest.post.PostConditionDto
import model.rest.post.PostTheoremDto
import service.ComputationService
import service.ValidationService
import util.reduceToNull
import java.time.ZonedDateTime
import java.util.*

@Path("/theorem")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class TheoremResource @Inject constructor(
    private val computationDao: ComputationDao,
    private val computationService: ComputationService,
    private val conclusionDao: ConclusionDao,
    private val conditionDao: ConditionDao,
    private val propertyDao: PropertyDao,
    private val referenceDao: ReferenceDao,
    private val theoremDao: TheoremDao,
    private val toDataMapper: ToDataMapper,
    private val toDtoMapper: ToDtoMapper,
    private val validationService: ValidationService
) {

    private val allowedFields = mapOf(
        Field.REAL to setOf(FieldLink.REAL, FieldLink.NOT_REAL),
        Field.COMPLEX to setOf(FieldLink.COMPLEX, FieldLink.NOT_COMPLEX),
        Field.REAL_OR_COMPLEX to setOf(
            FieldLink.REAL,
            FieldLink.NOT_REAL,
            FieldLink.COMPLEX,
            FieldLink.NOT_COMPLEX,
            FieldLink.REAL_AND_COMPLEX,
            FieldLink.NOT_REAL_AND_NOT_COMPLEX
        )
    )

    @GET
    fun get(@QueryParam("propertyId") propertyId: UUID?): Response {
        if (propertyId != null && propertyDao.get(propertyId) == null) {
            throw NotFoundException()
        } else {
            val theorems = theoremDao.getAll()
            val conditions = conditionDao.getAll().groupBy { it.theoremId }
            val conclusions = conclusionDao.getAll().groupBy { it.theoremId }
            val properties = propertyDao.getAll().associateBy { it.id }
            val references = referenceDao.getTheoremReferences().groupBy { it.theoremId }
            val theoremDtoList = theorems.map {
                toDtoMapper.toTheoremDto(
                    it,
                    conditions[it.id].orEmpty(),
                    conclusions[it.id].orEmpty(),
                    references[it.id].orEmpty(),
                    properties
                )
            }.filter {
                propertyId == null ||
                        it.conditions.any { condition -> condition.propertyId == propertyId } ||
                        it.conclusions.any { conclusion -> conclusion.propertyId == propertyId }
            }
            return Response.ok(theoremDtoList).build()
        }
    }

    @Path("/{id}")
    @GET
    fun getTheorem(@PathParam("id") id: UUID): Response {
        val theorem = theoremDao.get(id)
        if (theorem == null) {
            throw NotFoundException()
        } else {
            val conditions = conditionDao.getByTheoremId(id)
            val conclusions = conclusionDao.getByTheoremId(id)
            val properties = propertyDao.getAll().associateBy { it.id }
            val references = referenceDao.getByTheoremId(id)
            val theoremDto = toDtoMapper.toTheoremDto(
                theorem,
                conditions,
                conclusions,
                references,
                properties
            )
            return Response.ok(theoremDto).build()
        }
    }

    @POST
    fun add(@Auth user: User, postTheoremDto: PostTheoremDto): Response {
        val errors = validate(postTheoremDto)
        if (errors.isNotEmpty()) {
            throw JsonException(errors)
        } else {
            val now = ZonedDateTime.now()
            val theorem = TheoremData(
                id = UUID.randomUUID(),
                name = postTheoremDto.name.reduceToNull(),
                description = postTheoremDto.description.reduceToNull(),
                created = now,
                updated = now
            )
            try {
                theoremDao.create(theorem)
            } catch (exception: Exception) {
                throw JsonException(
                    key = "name",
                    value = "A theorem with this name already exists."
                )
            }
            val conditions = toDataMapper.toConditions(theorem.id, now, postTheoremDto.conditions)
            conditions.forEach(conditionDao::create)
            val conclusions = toDataMapper.toConclusions(theorem.id, now, postTheoremDto.conclusions)
            conclusions.forEach(conclusionDao::create)
            val references = toDataMapper.toTheoremReferences(theorem.id, now, postTheoremDto.references)
            references.forEach(referenceDao::create)
            val properties = propertyDao.getAll().associateBy { it.id }
            val theoremDto = toDtoMapper.toTheoremDto(theorem, conditions, conclusions, references, properties)
            computationService.compute()
            return Response.ok(theoremDto).build()
        }
    }

    @Path("/{id}")
    @POST
    fun update(@Auth user: User, @PathParam("id") id: UUID, postTheoremDto: PostTheoremDto): Response {
        val theorem = theoremDao.get(id)
        val errors = validate(postTheoremDto)
        if (theorem == null) {
            throw NotFoundException()
        } else if (errors.isNotEmpty()) {
            throw JsonException(errors)
        } else {
            val now = ZonedDateTime.now()
            val update = theorem.copy(
                name = postTheoremDto.name.reduceToNull(),
                description = postTheoremDto.description.reduceToNull(),
                updated = now
            )
            try {
                theoremDao.update(update)
            } catch (exception: Exception) {
                throw JsonException(
                    key = "name",
                    value = "Another theorem with this name already exists."
                )
            }
            conditionDao.deleteByTheoremId(id)
            val conditions = toDataMapper.toConditions(theorem.id, now, postTheoremDto.conditions)
            conditions.forEach(conditionDao::create)
            conclusionDao.deleteByTheoremId(id)
            val conclusions = toDataMapper.toConclusions(theorem.id, now, postTheoremDto.conclusions)
            conclusions.forEach(conclusionDao::create)
            referenceDao.deleteByTheoremId(id)
            val references = toDataMapper.toTheoremReferences(id, now, postTheoremDto.references)
            references.forEach(referenceDao::create)
            val properties = propertyDao.getAll().associateBy { it.id }
            val theoremDto = toDtoMapper.toTheoremDto(update, conditions, conclusions, references, properties)
            computationService.compute()
            return Response.ok(theoremDto).build()
        }
    }

    @Path("/{id}")
    @DELETE
    fun delete(@Auth user: User, @PathParam("id") id: UUID): Response {
        val theorem = theoremDao.get(id)
        if (theorem == null) {
            throw NotFoundException()
        } else {
            computationDao.deleteByTheoremId(id)
            conditionDao.deleteByTheoremId(id)
            conclusionDao.deleteByTheoremId(id)
            referenceDao.deleteByTheoremId(id)
            theoremDao.delete(id)
            computationService.compute()
            return Response.ok().build()
        }
    }

    private fun validate(postTheoremDto: PostTheoremDto): Map<String, String> {
        val errors = HashMap<String, String>()
        errors.putAll(validationService.validateIfNotBlank(postTheoremDto.name, "name", 128, false))
        errors.putAll(validationService.validateIfNotBlank(postTheoremDto.description, "description", 1024, true))
        errors.putAll(validationService.validateReferences(postTheoremDto.references))
        postTheoremDto.conditions.forEachIndexed { index, postConditionDto ->
            errors.putAll(validateCondition(index, postConditionDto))
        }
        postTheoremDto.conclusions.forEachIndexed { index, postConclusionDto ->
            errors.putAll(validateConclusion(index, postConclusionDto))
        }
        if (postTheoremDto.conditions.isEmpty()) {
            errors["general"] = "Conditions cannot be empty."
        } else if (postTheoremDto.conditions.size > 5) {
            errors["general"] = "Too many conditions."
        }
        if (postTheoremDto.conclusions.isEmpty()) {
            errors["general"] = "Conclusions cannot be empty."
        } else if (postTheoremDto.conclusions.size > 5) {
            errors["general"] = "Too many conclusions"
        }
        return errors
    }

    private fun validateCondition(index: Int, postConditionDto: PostConditionDto): Map<String, String> {
        val key = "condition[$index]"
        val property = propertyDao.get(postConditionDto.propertyId)
        return if (property == null) {
            mapOf(key to "Unknown property")
        } else if (!allowedFields[property.field].orEmpty().contains(postConditionDto.field)) {
            mapOf(key to "Invalid field")
        } else {
            emptyMap()
        }
    }

    private fun validateConclusion(index: Int, postConclusionDto: PostConclusionDto): Map<String, String> {
        val key = "conclusion[$index]"
        val property = propertyDao.get(postConclusionDto.propertyId)
        return if (property == null) {
            mapOf(key to "Unknown property")
        } else if (!allowedFields[property.field].orEmpty().contains(postConclusionDto.field)) {
            mapOf(key to "Invalid field")
        } else {
            emptyMap()
        }
    }

}
