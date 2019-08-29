package dao

import model.database.ReferenceData
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import java.util.*

interface ReferenceDao {

    @SqlUpdate(
        """
        INSERT INTO Reference (id, spaceId, propertyId, linkId, theoremId, title, url, arxivId, wikipediaId, bibtex, 
            page, statement, created, updated)
        VALUES (:data.id, :data.spaceId, :data.propertyId, :data.linkId, :data.theoremId, :data.title, :data.url, 
            :data.arxivId, :data.wikipediaId, :data.bibtex, :data.page, :data.statement, :data.created, :data.updated)
    """
    )
    fun create(data: ReferenceData)

    @SqlUpdate("DELETE FROM Reference WHERE spaceId = :spaceId")
    fun deleteBySpaceId(spaceId: UUID)

    @SqlUpdate("DELETE FROM Reference WHERE propertyId = :propertyId")
    fun deleteByPropertyId(propertyId: UUID)

    @SqlUpdate("DELETE FROM Reference WHERE linkId = :linkId")
    fun deleteByLinkId(linkId: UUID)

    @SqlUpdate("DELETE FROM Reference WHERE theoremId = :theoremId")
    fun deleteByTheoremId(theoremId: UUID)

    @SqlQuery("SELECT * FROM Reference WHERE spaceId = :spaceId")
    fun getBySpaceId(spaceId: UUID): List<ReferenceData>

    @SqlQuery("SELECT * FROM Reference WHERE propertyId = :propertyId")
    fun getByPropertyId(propertyId: UUID): List<ReferenceData>

    @SqlQuery("SELECT * FROM Reference WHERE linkId = :linkId")
    fun getByLinkId(linkId: UUID): List<ReferenceData>

    @SqlQuery("SELECT * FROM Reference WHERE theoremId = :theoremId")
    fun getByTheoremId(theoremId: UUID): List<ReferenceData>

    @SqlQuery("SELECT * FROM Reference WHERE theoremId IS NOT NULL")
    fun getTheoremReferences(): List<ReferenceData>

    @SqlQuery("SELECT * FROM Reference")
    fun getAll(): List<ReferenceData>

    @SqlUpdate("TRUNCATE TABLE Reference CASCADE")
    fun deleteAll()

}