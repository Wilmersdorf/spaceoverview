package dao

import model.database.DifferentialEquationLinkData
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import java.util.*

interface DifferentialEquationLinkDao {

    @SqlUpdate(
        """
        INSERT INTO DifferentialEquationLink (id, differentialEquationId, differentialEquationPropertyId, hasProperty, 
            description, created, updated)
        VALUES (:data.id, :data.differentialEquationId, :data.differentialEquationPropertyId, :data.hasProperty, 
            :data.description, :data.created, :data.updated)
    """
    )
    fun create(data: DifferentialEquationLinkData)

    @SqlUpdate(
        """
        UPDATE DifferentialEquationLink SET
        hasProperty = :data.hasProperty,
        description = :data.description,
        updated = :data.updated
        WHERE id = :data.id
    """
    )
    fun update(data: DifferentialEquationLinkData)

    @SqlUpdate("DELETE FROM DifferentialEquationLink WHERE id = :id")
    fun delete(id: UUID)

    @SqlQuery("SELECT * FROM DifferentialEquationLink WHERE id = :id")
    fun get(id: UUID): DifferentialEquationLinkData?

    @SqlQuery("SELECT * FROM DifferentialEquationLink WHERE differentialEquationId = :differentialEquationId")
    fun getByDifferentialEquationId(differentialEquationId: UUID): List<DifferentialEquationLinkData>

    @SqlQuery("SELECT * FROM DifferentialEquationLink WHERE differentialEquationPropertyId = :differentialEquationPropertyId")
    fun getByDifferentialEquationPropertyId(differentialEquationPropertyId: UUID): List<DifferentialEquationLinkData>

    @SqlQuery(
        """
        SELECT * FROM DifferentialEquationLink WHERE differentialEquationId = :differentialEquationId AND 
            differentialEquationPropertyId = :differentialEquationPropertyId
        """
    )
    fun getByDifferentialEquationIdAndDifferentialEquationPropertyId(
        differentialEquationId: UUID,
        differentialEquationPropertyId: UUID
    ): DifferentialEquationLinkData?

    @SqlQuery("SELECT * FROM DifferentialEquationLink")
    fun getAll(): List<DifferentialEquationLinkData>

    @SqlUpdate("TRUNCATE TABLE DifferentialEquationLink CASCADE")
    fun deleteAll()

}
