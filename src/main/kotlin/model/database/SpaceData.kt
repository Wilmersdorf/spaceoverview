package model.database

import model.enums.Field
import java.time.LocalDateTime
import java.util.*

data class SpaceData(
    val id: UUID,
    val symbol: String,
    val norm: String,
    val description: String,
    val field: Field,
    val created: LocalDateTime,
    val updated: LocalDateTime
)