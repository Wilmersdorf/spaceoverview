package service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.inject.Inject
import dao.LinkDao
import dao.PropertyDao
import dao.ReferenceDao
import dao.SpaceDao
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
    private val referenceDao: ReferenceDao
) {

    fun export(): File {
        val spaces = spaceDao.getAll()
        val properties = propertyDao.getAll()
        val links = linkDao.getAll()
        val references = referenceDao.getAll()
        val backup = Backup(spaces, properties, links, references)
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
            spaceDao.deleteAll()
            propertyDao.deleteAll()
            linkDao.deleteAll()
            referenceDao.deleteAll()
            backup.spaces?.forEach(spaceDao::create)
            backup.properties?.forEach(propertyDao::create)
            backup.links?.forEach(linkDao::create)
            backup.references?.forEach(referenceDao::create)
        }
    }

}