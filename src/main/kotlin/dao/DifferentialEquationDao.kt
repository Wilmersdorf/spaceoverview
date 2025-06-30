package dao

import model.database.DifferentialEquationData
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import java.util.*

interface DifferentialEquationDao {

    @SqlUpdate(
        """
        INSERT INTO DifferentialEquation (
        id,
        name,
        symbol,
        description,
        variables,
        parameters,
        created,
        updated
        )
        VALUES (
        :data.id,
        :data.name,
        :data.symbol,
        :data.description,
        :data.variables,
        :data.parameters,
        :data.created,
        :data.updated
        )
    """
    )
    fun create(data: DifferentialEquationData)

    @SqlUpdate(
        """
        UPDATE DifferentialEquation SET
        id = :data.id,
        name = :data.name,
        symbol = :data.symbol,
        description = :data.description,
        variables = :data.variables,
        parameters = :data.parameters,
        created = :data.created,
        updated = :data.updated
        WHERE id = :data.id
    """
    )
    fun update(data: DifferentialEquationData)

    @SqlQuery("SELECT * FROM DifferentialEquation WHERE id = :id")
    fun get(id: UUID): DifferentialEquationData?

    @SqlQuery("SELECT * FROM DifferentialEquation")
    fun getAll(): List<DifferentialEquationData>

    @SqlUpdate("DELETE FROM DifferentialEquation WHERE id = :id")
    fun delete(id: UUID)

    @SqlUpdate("TRUNCATE TABLE DifferentialEquation CASCADE")
    fun deleteAll()

}
