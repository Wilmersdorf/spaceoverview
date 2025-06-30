package service

import com.google.inject.Inject
import dao.*
import model.database.ComputationData
import model.database.ConditionData
import model.database.LinkData
import util.Logging
import java.time.ZonedDateTime
import java.util.*

class ComputationService @Inject constructor(
    private val computationDao: ComputationDao,
    private val conclusionDao: ConclusionDao,
    private val conditionDao: ConditionDao,
    private val fieldService: FieldService,
    private val linkDao: LinkDao,
    private val spaceDao: SpaceDao,
    private val theoremDao: TheoremDao
) {

    private val logger = Logging.logger {}

    @Synchronized
    fun compute(): List<ComputationData> {
        val now = ZonedDateTime.now()
        val time = System.currentTimeMillis()
        computationDao.deleteAll()
        val spaces = spaceDao.getAll()
        val spaceIdToLink = linkDao.getAll().groupBy { it.spaceId }
        val theorems = theoremDao.getAll()
        val theoremIdToConditions = conditionDao.getAll().groupBy { it.theoremId }
        val theoremIdToConclusions = conclusionDao.getAll().groupBy { it.theoremId }
        val computations = ArrayList<ComputationData>()
        for (space in spaces) {
            val spaceLinks = spaceIdToLink[space.id] ?: continue
            for (theorem in theorems) {
                val theoremConditions = theoremIdToConditions[theorem.id].orEmpty()
                val real = theoremConditions.all { spaceHasConditionReal(spaceLinks, it) }
                val complex = theoremConditions.all { spaceHasConditionComplex(spaceLinks, it) }
                theoremIdToConclusions[theorem.id].orEmpty().forEach { conclusion ->
                    val realConclusion = fieldService.real(conclusion.field)
                    val complexConclusion = fieldService.complex(conclusion.field)
                    val spaceRealConclusion = if (real && realConclusion != null) {
                        realConclusion
                    } else {
                        null
                    }
                    val spaceComplexConclusion = if (complex && complexConclusion != null) {
                        complexConclusion
                    } else {
                        null
                    }
                    if (spaceRealConclusion != null || spaceComplexConclusion != null) {
                        computations.add(
                            ComputationData(
                                id = UUID.randomUUID(),
                                spaceId = space.id,
                                theoremId = conclusion.theoremId,
                                propertyId = conclusion.propertyId,
                                field = fieldService.merge(Pair(spaceRealConclusion, spaceComplexConclusion))!!,
                                created = now,
                                updated = now
                            )
                        )
                    }
                }
            }
        }
        computations.forEach(computationDao::create)
        logger.info("Computation took ${System.currentTimeMillis() - time} milliseconds")
        return computations
    }

    private fun spaceHasConditionReal(links: List<LinkData>, condition: ConditionData): Boolean {
        val link = links.firstOrNull { it.propertyId == condition.propertyId } ?: return false
        val realLink = fieldService.real(link.field) ?: return false
        val realCondition = fieldService.real(condition.field) ?: return false
        return realLink == realCondition
    }

    private fun spaceHasConditionComplex(links: List<LinkData>, condition: ConditionData): Boolean {
        val link = links.firstOrNull { it.propertyId == condition.propertyId } ?: return false
        val complexLink = fieldService.complex(link.field) ?: return false
        val complexCondition = fieldService.complex(condition.field) ?: return false
        return complexLink == complexCondition
    }

}
