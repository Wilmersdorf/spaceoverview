package model.rest

import model.enums.Field
import java.time.LocalDateTime
import java.util.*

class PropertyDto(
    val id: UUID,
    val name: String,
    val description: String,
    val field: Field,
    val created: LocalDateTime,
    val updated: LocalDateTime,
    val references: List<ReferenceDto>
)