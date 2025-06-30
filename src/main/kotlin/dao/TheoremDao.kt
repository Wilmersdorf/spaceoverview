package dao

import model.database.TheoremData
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import java.util.*

interface TheoremDao {

    @SqlUpdate(
        """
        INSERT INTO Theorem (
        id,
        name,
        description,
        created,
        updated
        )
        VALUES (
        :data.id,
        :data.name,
        :data.description,
        :data.created,
        :data.updated
        )
    """
    )
    fun create(data: TheoremData)

    @SqlUpdate(
        """
        UPDATE Theorem SET
        id = :data.id,
        name = :data.name,
        description = :data.description,
        created = :data.created,
        updated = :data.updated
        WHERE id = :data.id
    """
    )
    fun update(data: TheoremData)

    @SqlQuery("SELECT * FROM Theorem WHERE id = :id")
    fun get(id: UUID): TheoremData?

    @SqlQuery("SELECT * FROM Theorem")
    fun getAll(): List<TheoremData>

    @SqlUpdate("DELETE FROM Theorem WHERE id = :id")
    fun delete(id: UUID)

    @SqlUpdate("TRUNCATE TABLE Theorem CASCADE")
    fun deleteAll()

}
