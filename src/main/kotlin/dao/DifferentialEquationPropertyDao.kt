package dao

import model.database.DifferentialEquationPropertyData
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import java.util.*

interface DifferentialEquationPropertyDao {

    @SqlUpdate(
        """
        INSERT INTO DifferentialEquationProperty (
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
    fun create(data: DifferentialEquationPropertyData)

    @SqlUpdate(
        """
        UPDATE DifferentialEquationProperty SET
        id = :data.id,
        name = :data.name,
        description = :data.description,
        created = :data.created,
        updated = :data.updated
        WHERE id = :data.id
    """
    )
    fun update(data: DifferentialEquationPropertyData)

    @SqlUpdate("DELETE FROM DifferentialEquationProperty WHERE id = :id")
    fun delete(id: UUID)

    @SqlQuery("SELECT * FROM DifferentialEquationProperty WHERE id = :id")
    fun get(id: UUID): DifferentialEquationPropertyData?

    @SqlQuery("SELECT * FROM DifferentialEquationProperty")
    fun getAll(): List<DifferentialEquationPropertyData>

    @SqlUpdate("TRUNCATE TABLE DifferentialEquationProperty CASCADE")
    fun deleteAll()

}
