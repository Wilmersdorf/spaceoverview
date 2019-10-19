package model.rest

import java.time.LocalDateTime
import java.util.*

data class DifferentialEquationLinkDto(
    val id: UUID,
    val differentialEquationId: UUID,
    val differentialEquationPropertyId: UUID,
    val hasProperty: Boolean,
    val description: String?,
    val created: LocalDateTime,
    val updated: LocalDateTime,
    val references: List<ReferenceDto>
)
