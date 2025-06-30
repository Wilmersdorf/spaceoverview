package service

import com.google.inject.Inject
import dao.*
import model.rest.post.PostBackupDto

class BackupService @Inject constructor(
    private val computationDao: ComputationDao,
    private val conclusionDao: ConclusionDao,
    private val conditionDao: ConditionDao,
    private val differentialEquationDao: DifferentialEquationDao,
    private val differentialEquationLinkDao: DifferentialEquationLinkDao,
    private val differentialEquationPropertyDao: DifferentialEquationPropertyDao,
    private val linkDao: LinkDao,
    private val propertyDao: PropertyDao,
    private val referenceDao: ReferenceDao,
    private val spaceDao: SpaceDao,
    private val theoremDao: TheoremDao
) {

    fun export(): PostBackupDto {
        val spaces = spaceDao.getAll()
        val properties = propertyDao.getAll()
        val links = linkDao.getAll()
        val theorems = theoremDao.getAll()
        val conditions = conditionDao.getAll()
        val conclusions = conclusionDao.getAll()
        val references = referenceDao.getAll()
        val differentialEquations = differentialEquationDao.getAll()
        val differentialEquationProperties = differentialEquationPropertyDao.getAll()
        val differentialEquationLinks = differentialEquationLinkDao.getAll()
        return PostBackupDto(
            spaces = spaces,
            properties = properties,
            links = links,
            theorems = theorems,
            conditions = conditions,
            conclusions = conclusions,
            differentialEquations = differentialEquations,
            differentialEquationProperties = differentialEquationProperties,
            differentialEquationLinks = differentialEquationLinks,
            references = references
        )
    }

    fun import(backup: PostBackupDto) {
        computationDao.deleteAll()
        referenceDao.deleteAll()
        linkDao.deleteAll()
        conditionDao.deleteAll()
        conclusionDao.deleteAll()
        theoremDao.deleteAll()
        spaceDao.deleteAll()
        propertyDao.deleteAll()
        differentialEquationLinkDao.deleteAll()
        differentialEquationDao.deleteAll()
        differentialEquationPropertyDao.deleteAll()
        backup.spaces?.forEach(spaceDao::create)
        backup.properties?.forEach(propertyDao::create)
        backup.links?.forEach(linkDao::create)
        backup.theorems?.forEach(theoremDao::create)
        backup.conditions?.forEach(conditionDao::create)
        backup.conclusions?.forEach(conclusionDao::create)
        backup.differentialEquations?.forEach(differentialEquationDao::create)
        backup.differentialEquationProperties?.forEach(differentialEquationPropertyDao::create)
        backup.differentialEquationLinks?.forEach(differentialEquationLinkDao::create)
        backup.references?.forEach(referenceDao::create)
    }

}
