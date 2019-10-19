package model.rest

import java.time.LocalDateTime
import java.util.*

data class DifferentialEquationDto(
    val id: UUID,
    val name: String,
    val symbol: String,
    val description: String,
    val variables: String,
    val parameters: String,
    val created: LocalDateTime,
    val updated: LocalDateTime,
    val references: List<ReferenceDto>
)
