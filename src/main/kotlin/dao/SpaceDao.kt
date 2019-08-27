package dao

import model.database.SpaceData
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import java.util.*

interface SpaceDao {

    @SqlUpdate(
        """
        INSERT INTO SpaceData (id, symbol, norm, description, field, created, updated)
        VALUES (:data.id, :data.symbol, :data.norm, :data.description, :data.field, :data.created, :data.updated)
    """
    )
    fun create(data: SpaceData)

    @SqlUpdate(
        """
        UPDATE SpaceData SET
        symbol = :data.symbol, 
        norm = :data.norm,
        description = :data.description,
        field = :data.field,
        updated = :data.updated
        WHERE id = :data.id
    """
    )
    fun update(data: SpaceData)

    @SqlUpdate("DELETE FROM SpaceData WHERE id = :id")
    fun delete(id: UUID)

    @SqlQuery("SELECT * FROM SpaceData WHERE id = :id")
    fun get(id: UUID): SpaceData?

    @SqlQuery("SELECT * FROM SpaceData")
    fun getAll(): List<SpaceData>

    @SqlUpdate("TRUNCATE TABLE SpaceData CASCADE")
    fun deleteAll()

}