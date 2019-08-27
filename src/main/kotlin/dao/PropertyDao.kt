package dao

import model.database.PropertyData
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import java.util.*

interface PropertyDao {

    @SqlUpdate(
        """
        INSERT INTO Property (id, name, description, field, created, updated)
        VALUES (:data.id, :data.name, :data.description, :data.field, :data.created, :data.updated)
    """
    )
    fun create(data: PropertyData)

    @SqlUpdate(
        """
        UPDATE Property SET
        name = :data.name,
        description = :data.description,
        field = :data.field,
        updated = :data.updated
        WHERE id = :data.id
    """
    )
    fun update(data: PropertyData)

    @SqlUpdate("DELETE FROM Property WHERE id = :id")
    fun delete(id: UUID)

    @SqlQuery("SELECT * FROM Property WHERE id = :id")
    fun get(id: UUID): PropertyData?

    @SqlQuery("SELECT * FROM Property")
    fun getAll(): List<PropertyData>

    @SqlUpdate("TRUNCATE TABLE Property CASCADE")
    fun deleteAll()

}