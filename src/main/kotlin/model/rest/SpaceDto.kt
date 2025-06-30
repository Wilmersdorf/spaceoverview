package model.rest

import model.enums.Field
import java.time.ZonedDateTime
import java.util.*

data class SpaceDto(
    val id: UUID,
    val symbol: String,
    val norm: String,
    val description: String,
    val field: Field,
    val created: ZonedDateTime,
    val updated: ZonedDateTime,
    val references: List<ReferenceDto>
)
