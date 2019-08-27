package model.database


import java.time.LocalDateTime
import java.util.*

data class InviteData(
    val id: UUID,
    val code: String,
    val created: LocalDateTime,
    val updated: LocalDateTime
)