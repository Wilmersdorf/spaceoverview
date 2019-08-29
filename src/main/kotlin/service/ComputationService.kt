package service

import com.google.inject.Inject
import dao.*
import model.database.*
import model.enums.Field
import model.enums.FieldLink
import model.enums.FieldLink.*
import mu.KotlinLogging
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList

class ComputationService @Inject constructor(
    private val computationDao: ComputationDao,
    private val spaceDao: SpaceDao,
    private val linkDao: LinkDao,
    private val theoremDao: TheoremDao,
    private val conditionDao: ConditionDao,
    private val conclusionDao: ConclusionDao
) {

    private val logger = KotlinLogging.logger {}

    private val fieldLinkMap = mapOf(
        REAL to setOf(REAL, REAL_AND_COMPLEX, REAL_AND_NOT_COMPLEX),
        COMPLEX to setOf(COMPLEX, REAL_AND_COMPLEX, NOT_REAL_AND_COMPLEX),
        REAL_AND_COMPLEX to setOf(REAL_AND_COMPLEX),
        NOT_REAL to setOf(NOT_REAL, NOT_REAL_AND_NOT_COMPLEX, NOT_REAL_AND_COMPLEX),
        NOT_COMPLEX to setOf(NOT_COMPLEX, NOT_REAL_AND_NOT_COMPLEX, REAL_AND_NOT_COMPLEX),
        NOT_REAL_AND_NOT_COMPLEX to setOf(NOT_REAL_AND_COMPLEX)
    )

    @Synchronized
    fun compute(): List<ComputationData> {
        val time = System.currentTimeMillis()
        computationDao.deleteAll()
        val spaces = spaceDao.getAll()
        val links = linkDao.getAll()
        val linksBySpace = links.groupBy { it.spaceId }
        val theorems = theoremDao.getAll()
        val conditions = conditionDao.getAll()
        val conditionsByTheorem = conditions.groupBy { it.theoremId }
        val conclusions = conclusionDao.getAll()
        val conclusionsByTheorem = conclusions.groupBy { it.theoremId }
        val computations = ArrayList<ComputationData>()
        for (space in spaces) {
            val spaceLinks = linksBySpace[space.id].orEmpty()
            for (theorem in theorems) {
                val theoremConditions = conditionsByTheorem[theorem.id].orEmpty()
                val hasConditions = theoremConditions.all {
                    spaceHasCondition(spaceLinks, it)
                }
                if (hasConditions) {
                    val theoremConclusions = conclusionsByTheorem[theorem.id].orEmpty()
                    computations.addAll(getComputations(space, theoremConclusions))
                }
            }
        }
        computations.forEach(computationDao::create)
        logger.info { "Computation took ${System.currentTimeMillis() - time} milliseconds" }
        return computations
    }

    private fun spaceHasCondition(links: List<LinkData>, condition: ConditionData): Boolean {
        val link = links.firstOrNull { it.propertyId == condition.propertyId }
        return link != null && fieldLinkMap[condition.field].orEmpty().contains(link.field)
    }

    private fun getComputations(space: SpaceData, conclusions: List<ConclusionData>): List<ComputationData> {
        val now = LocalDateTime.now()
        return conclusions.mapNotNull { getConclusionField(space, it) }.map {
            ComputationData(
                id = UUID.randomUUID(),
                spaceId = space.id,
                theoremId = it.first.theoremId,
                propertyId = it.first.propertyId,
                field = it.second,
                created = now,
                updated = now
            )
        }.toList()
    }

    private fun getConclusionField(space: SpaceData, conclusion: ConclusionData): Pair<ConclusionData, FieldLink>? {
        return if (space.field == Field.REAL) {
            if (fieldLinkMap[REAL].orEmpty().contains(conclusion.field)) {
                Pair(conclusion, REAL)
            } else if (fieldLinkMap[NOT_REAL].orEmpty().contains(conclusion.field)) {
                Pair(conclusion, NOT_REAL)
            } else {
                null
            }
        } else if (space.field == Field.COMPLEX) {
            if (fieldLinkMap[COMPLEX].orEmpty().contains(conclusion.field)) {
                Pair(conclusion, COMPLEX)
            } else if (fieldLinkMap[NOT_COMPLEX].orEmpty().contains(conclusion.field)) {
                Pair(conclusion, NOT_COMPLEX)
            } else {
                null
            }
        } else if (space.field == Field.REAL_OR_COMPLEX) {
            return Pair(conclusion, conclusion.field)
        } else {
            null
        }
    }

}
