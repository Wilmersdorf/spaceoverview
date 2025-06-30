package service

import com.google.inject.Inject
import dao.UserDao
import model.database.UserData
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.time.ZonedDateTime
import java.util.*

class DevStartupService @Inject constructor(
    private val bCryptPasswordEncoder: BCryptPasswordEncoder,
    private val userDao: UserDao
) {

    fun startup() {
        val users = userDao.getAll()
        if (users.isEmpty()) {
            val now = ZonedDateTime.now()
            val adminHash = bCryptPasswordEncoder.encode("password")
            val admin = UserData(
                id = UUID.randomUUID(),
                email = "admin@test.space",
                hash = adminHash,
                isAdmin = true,
                created = now,
                updated = now
            )
            userDao.create(admin)
            val userHash = bCryptPasswordEncoder.encode("password")
            val user = UserData(
                id = UUID.randomUUID(),
                email = "user@test.space",
                hash = userHash,
                isAdmin = false,
                created = now,
                updated = now
            )
            userDao.create(user)
        }
    }

}
