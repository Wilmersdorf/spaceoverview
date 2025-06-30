package dao

import model.database.ConclusionData
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import java.util.*

interface ConclusionDao {

    @SqlUpdate(
        """
        INSERT INTO Conclusion (
        id,
        theoremId,
        propertyId,
        field,
        created,
        updated
        )
        VALUES (
        :data.id,
        :data.theoremId,
        :data.propertyId,
        :data.field,
        :data.created,
        :data.updated
        )
    """
    )
    fun create(data: ConclusionData)

    @SqlQuery("SELECT * FROM Conclusion WHERE theoremId = :theoremId")
    fun getByTheoremId(theoremId: UUID): List<ConclusionData>

    @SqlQuery("SELECT * FROM Conclusion WHERE propertyId = :propertyId")
    fun getByPropertyId(propertyId: UUID): List<ConclusionData>

    @SqlQuery("SELECT * FROM Conclusion")
    fun getAll(): List<ConclusionData>

    @SqlUpdate("DELETE FROM Conclusion WHERE theoremId = :theoremId")
    fun deleteByTheoremId(theoremId: UUID)

    @SqlUpdate("TRUNCATE TABLE Conclusion CASCADE")
    fun deleteAll()

}
