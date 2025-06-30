package model.rest

import java.time.ZonedDateTime
import java.util.*

data class DifferentialEquationPropertyDto(
    val id: UUID,
    val name: String,
    val description: String,
    val created: ZonedDateTime,
    val updated: ZonedDateTime,
    val references: List<ReferenceDto>
)
