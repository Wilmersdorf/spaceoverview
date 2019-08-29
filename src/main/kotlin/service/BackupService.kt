package service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.inject.Inject
import dao.*
import model.Backup
import org.apache.commons.io.FileUtils
import org.glassfish.jersey.media.multipart.FormDataBodyPart
import java.io.File
import java.io.InputStream
import java.nio.charset.Charset
import java.time.LocalDateTime

class BackupService @Inject constructor(
    private val mapper: ObjectMapper,
    private val spaceDao: SpaceDao,
    private val linkDao: LinkDao,
    private val propertyDao: PropertyDao,
    private val theoremDao: TheoremDao,
    private val conditionDao: ConditionDao,
    private val conclusionDao: ConclusionDao,
    private val referenceDao: ReferenceDao,
    private val computationDao: ComputationDao
) {

    fun export(): File {
        val spaces = spaceDao.getAll()
        val properties = propertyDao.getAll()
        val links = linkDao.getAll()
        val theorems = theoremDao.getAll()
        val conditions = conditionDao.getAll()
        val conclusions = conclusionDao.getAll()
        val references = referenceDao.getAll()
        val backup = Backup(spaces, properties, links, theorems, conditions, conclusions, references)
        val json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(backup)
        val file = File.createTempFile(LocalDateTime.now().toString() + "-backup", ".json")
        FileUtils.writeStringToFile(file, json, Charset.defaultCharset())
        return file
    }

    fun import(body: FormDataBodyPart) {
        val files = body.parent.bodyParts
            .filter { it.contentDisposition.parameters["name"] == "file" }
        val file = files.firstOrNull()
        if (file != null) {
            val stream = file.getEntityAs(InputStream::class.java)
            val backup = mapper.readValue<Backup>(stream)
            computationDao.deleteAll()
            referenceDao.deleteAll()
            linkDao.deleteAll()
            conditionDao.deleteAll()
            conclusionDao.deleteAll()
            theoremDao.deleteAll()
            spaceDao.deleteAll()
            propertyDao.deleteAll()
            backup.spaces?.forEach(spaceDao::create)
            backup.properties?.forEach(propertyDao::create)
            backup.links?.forEach(linkDao::create)
            backup.theorems?.forEach(theoremDao::create)
            backup.conditions?.forEach(conditionDao::create)
            backup.conclusions?.forEach(conclusionDao::create)
            backup.references?.forEach(referenceDao::create)
        }
    }

}