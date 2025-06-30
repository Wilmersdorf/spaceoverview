package model.rest

import model.enums.Field
import java.time.ZonedDateTime
import java.util.*

class PropertyDto(
    val id: UUID,
    val name: String,
    val description: String,
    val field: Field,
    val created: ZonedDateTime,
    val updated: ZonedDateTime,
    val references: List<ReferenceDto>
)
