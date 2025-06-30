package model.rest

import java.time.ZonedDateTime
import java.util.*

data class DifferentialEquationDto(
    val id: UUID,
    val name: String,
    val symbol: String,
    val description: String,
    val variables: String,
    val parameters: String,
    val created: ZonedDateTime,
    val updated: ZonedDateTime,
    val references: List<ReferenceDto>
)
