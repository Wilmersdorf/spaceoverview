package model.database

import java.time.LocalDateTime
import java.util.*

data class TheoremData(
    val id: UUID,
    val name: String?,
    val created: LocalDateTime,
    val updated: LocalDateTime
)
