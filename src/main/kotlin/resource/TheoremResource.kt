package resource

import com.google.inject.Inject
import dao.*
import extensions.reduceToNull
import io.dropwizard.auth.Auth
import mapper.ToDataMapper
import mapper.ToDtoMapper
import model.User
import model.database.TheoremData
import model.enums.Field
import model.enums.FieldLink
import model.rest.ErrorDto
import model.rest.post.PostConclusionDto
import model.rest.post.PostConditionDto
import model.rest.post.PostTheoremDto
import service.ComputationService
import service.ValidationService
import java.time.LocalDateTime
import java.util.*
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status.NOT_FOUND
import kotlin.collections.HashMap

@Path("/theorem")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class TheoremResource @Inject constructor(
    private val validationService: ValidationService,
    private val theoremDao: TheoremDao,
    private val conditionDao: ConditionDao,
    private val conclusionDao: ConclusionDao,
    private val propertyDao: PropertyDao,
    private val referenceDao: ReferenceDao,
    private val toDataMapper: ToDataMapper,
    private val toDtoMapper: ToDtoMapper,
    private val computationDao: ComputationDao,
    private val computationService: ComputationService
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
            return Response.status(NOT_FOUND).build()
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
            return Response.status(NOT_FOUND).build()
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
    fun add(postTheoremDto: PostTheoremDto, @Auth user: User): Response {
        val errors = validate(postTheoremDto)
        return if (errors.isNotEmpty()) {
            Response.status(Response.Status.BAD_REQUEST).entity(ErrorDto(errors)).build()
        } else {
            val now = LocalDateTime.now()
            val theorem = TheoremData(
                id = UUID.randomUUID(),
                name = postTheoremDto.name.reduceToNull(),
                created = now,
                updated = now
            )
            try {
                theoremDao.create(theorem)
            } catch (exception: Exception) {
                val duplicateError = mapOf("name" to "A theorem with this name already exists.")
                return Response.status(Response.Status.BAD_REQUEST).entity(ErrorDto(duplicateError)).build()
            }
            val conditions = toDataMapper.toConditions(theorem.id, now, postTheoremDto.conditions)
            conditions.forEach(conditionDao::create)
            val conclusions = toDataMapper.toConclusions(theorem.id, now, postTheoremDto.conclusions)
            conclusions.forEach(conclusionDao::create)
            val references =
                toDataMapper.toTheoremReferences(
                    theoremId = theorem.id,
                    time = now,
                    references = postTheoremDto.references
                )
            references.forEach(referenceDao::create)
            val properties = propertyDao.getAll().associateBy { it.id }
            val theoremDto = toDtoMapper.toTheoremDto(theorem, conditions, conclusions, references, properties)
            computationService.compute()
            Response.ok(theoremDto).build()
        }
    }

    @Path("/{id}")
    @POST
    fun update(
        @PathParam("id") id: UUID,
        postTheoremDto: PostTheoremDto,
        @Auth user: User
    ): Response {
        val theorem = theoremDao.get(id)
        val errors = validate(postTheoremDto)
        return if (theorem == null) {
            return Response.status(NOT_FOUND).build()
        } else if (errors.isNotEmpty()) {
            Response.status(Response.Status.BAD_REQUEST).entity(ErrorDto(errors)).build()
        } else {
            val now = LocalDateTime.now()
            val theoremUpdate = theorem.copy(
                name = postTheoremDto.name.reduceToNull(),
                updated = now
            )
            try {
                theoremDao.update(theoremUpdate)
            } catch (exception: Exception) {
                val duplicateError = mapOf("name" to "Another theorem with this name already exists.")
                return Response.status(Response.Status.BAD_REQUEST).entity(ErrorDto(duplicateError)).build()
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
            val theoremDto = toDtoMapper.toTheoremDto(theoremUpdate, conditions, conclusions, references, properties)
            computationService.compute()
            Response.ok(theoremDto).build()
        }
    }

    @Path("/{id}")
    @DELETE
    fun delete(@PathParam("id") id: UUID, @Auth user: User): Response {
        val theorem = theoremDao.get(id)
        return if (theorem == null) {
            return Response.status(NOT_FOUND).build()
        } else {
            computationDao.deleteByTheoremId(id)
            conditionDao.deleteByTheoremId(id)
            conclusionDao.deleteByTheoremId(id)
            referenceDao.deleteByTheoremId(id)
            theoremDao.delete(id)
            computationService.compute()
            Response.ok().build()
        }
    }

    private fun validate(postTheoremDto: PostTheoremDto): Map<String, String> {
        val errors = HashMap<String, String>()
        errors.putAll(validationService.validateIfNotBlank(postTheoremDto.name, "name", 128))
        errors.putAll(validationService.validateReferences(postTheoremDto.references))
        postTheoremDto.conditions.forEachIndexed { index, postConditionDto ->
            errors.putAll(validateCondition(index, postConditionDto))
        }
        postTheoremDto.conclusions.forEachIndexed { index, postConclusionDto ->
            errors.putAll(validateConclusion(index, postConclusionDto))
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
