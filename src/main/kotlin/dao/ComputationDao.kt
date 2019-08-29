package dao

import model.database.ComputationData
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import java.util.*

interface ComputationDao {

    @SqlUpdate(
        """
        INSERT INTO Computation (id, spaceId, propertyId, theoremId, field, created, updated)
        VALUES (:data.id, :data.spaceId, :data.propertyId, :data.theoremId, :data.field, :data.created, :data.updated)
    """
    )
    fun create(data: ComputationData)

    @SqlQuery("SELECT * FROM Computation")
    fun getAll(): List<ComputationData>

    @SqlQuery("SELECT * FROM Computation WHERE spaceId = :spaceId")
    fun getBySpaceId(spaceId: UUID): List<ComputationData>

    @SqlQuery("SELECT * FROM Computation WHERE propertyId = :propertyId")
    fun getByPropertyId(propertyId: UUID): List<ComputationData>

    @SqlQuery("SELECT * FROM Computation WHERE spaceId = :spaceId AND propertyId = :propertyId")
    fun getBySpaceIdAndPropertyId(spaceId: UUID, propertyId: UUID): List<ComputationData>

    @SqlUpdate("DELETE FROM Computation WHERE theoremId = :theoremId")
    fun deleteByTheoremId(theoremId: UUID)

    @SqlUpdate("TRUNCATE TABLE Computation")
    fun deleteAll()

}
