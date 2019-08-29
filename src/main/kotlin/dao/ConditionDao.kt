package dao

import model.database.ConditionData
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import java.util.*

interface ConditionDao {

    @SqlUpdate(
        """
        INSERT INTO Condition (id, theoremId, propertyId, field, created, updated)
        VALUES (:data.id, :data.theoremId, :data.propertyId, :data.field, :data.created, :data.updated)
    """
    )
    fun create(data: ConditionData)

    @SqlQuery("SELECT * FROM Condition WHERE id = :id")
    fun get(id: UUID): ConditionData?

    @SqlQuery("SELECT * FROM Condition WHERE theoremId = :theoremId")
    fun getByTheoremId(theoremId: UUID): List<ConditionData>

    @SqlQuery("SELECT * FROM Condition WHERE propertyId = :propertyId")
    fun getByPropertyId(propertyId: UUID): List<ConditionData>

    @SqlQuery("SELECT * FROM Condition")
    fun getAll(): List<ConditionData>

    @SqlUpdate("DELETE FROM Condition WHERE theoremId = :theoremId")
    fun deleteByTheoremId(theoremId: UUID)

    @SqlUpdate("TRUNCATE TABLE Condition CASCADE")
    fun deleteAll()

}
