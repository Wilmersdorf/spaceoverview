package dao

import model.database.LinkData
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import java.util.*

interface LinkDao {

    @SqlUpdate(
        """
        INSERT INTO Link (
        id,
        spaceId,
        propertyId,
        field,
        description,
        created,
        updated
        )
        VALUES (
        :data.id,
        :data.spaceId,
        :data.propertyId,
        :data.field,
        :data.description,
        :data.created,
        :data.updated
        )
    """
    )
    fun create(data: LinkData)

    @SqlUpdate(
        """
        UPDATE Link SET
        id = :data.id,
        spaceId = :data.spaceId,
        propertyId = :data.propertyId,
        field = :data.field,
        description = :data.description,
        created = :data.created,
        updated = :data.updated
        WHERE id = :data.id
    """
    )
    fun update(data: LinkData)

    @SqlQuery("SELECT * FROM Link WHERE spaceId = :spaceId")
    fun getBySpaceId(spaceId: UUID): List<LinkData>

    @SqlQuery("SELECT * FROM Link WHERE propertyId = :propertyId")
    fun getByPropertyId(propertyId: UUID): List<LinkData>

    @SqlQuery("SELECT * FROM Link WHERE spaceId = :spaceId AND propertyId = :propertyId")
    fun getBySpaceIdAndPropertyId(spaceId: UUID, propertyId: UUID): LinkData?

    @SqlQuery("SELECT * FROM Link")
    fun getAll(): List<LinkData>

    @SqlUpdate("DELETE FROM Link WHERE id = :id")
    fun delete(id: UUID)

    @SqlUpdate("TRUNCATE TABLE Link CASCADE")
    fun deleteAll()

}
