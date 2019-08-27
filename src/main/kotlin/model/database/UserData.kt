package model.database

import java.time.LocalDateTime
import java.util.*

data class UserData(
    val id: UUID,
    val email: String,
    val hash: String,
    val isAdmin: Boolean,
    val created: LocalDateTime,
    val updated: LocalDateTime
)
