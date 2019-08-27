package dao

import model.database.UserData
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import java.util.*

interface UserDao {

    @SqlUpdate(
        """
        INSERT INTO UserData (id, email, hash, isAdmin, created, updated)
        VALUES (:data.id, :data.email, :data.hash, :data.isAdmin, :data.created, :data.updated)
    """
    )
    fun create(data: UserData)

    @SqlQuery("SELECT * FROM UserData WHERE id = :id")
    fun get(id: UUID): UserData?

    @SqlQuery("SELECT * FROM UserData WHERE email = :email")
    fun getByEmail(email: String): UserData?

    @SqlQuery("SELECT * From UserData")
    fun getAll(): List<UserData>

}