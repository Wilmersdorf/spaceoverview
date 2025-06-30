package dao

import model.database.InviteData
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import java.util.*

interface InviteDao {

    @SqlUpdate(
        """
        INSERT INTO Invite (
        id,
        code,
        created,
        updated
        )
        VALUES (
        :data.id,
        :data.code,
        :data.created,
        :data.updated
        )
    """
    )
    fun create(data: InviteData)

    @SqlQuery("SELECT * FROM Invite WHERE code = :code")
    fun getByCode(code: String): InviteData?

    @SqlUpdate("DELETE FROM Invite WHERE id = :id")
    fun delete(id: UUID)

}
