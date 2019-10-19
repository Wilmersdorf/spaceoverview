package model.rest

import java.time.LocalDateTime
import java.util.*

data class DifferentialEquationPropertyDto(
    val id: UUID,
    val name: String,
    val description: String,
    val created: LocalDateTime,
    val updated: LocalDateTime,
    val references: List<ReferenceDto>
)
