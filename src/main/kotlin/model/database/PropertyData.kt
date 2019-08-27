package model.database

import model.enums.Field
import java.time.LocalDateTime
import java.util.*

data class PropertyData(
    val id: UUID,
    val name: String,
    val description: String,
    val field: Field,
    val created: LocalDateTime,
    val updated: LocalDateTime
)