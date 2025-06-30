package model.rest

import java.time.ZonedDateTime
import java.util.*

data class DifferentialEquationLinkDto(
    val id: UUID,
    val differentialEquationId: UUID,
    val differentialEquationPropertyId: UUID,
    val hasProperty: Boolean,
    val description: String?,
    val created: ZonedDateTime,
    val updated: ZonedDateTime,
    val references: List<ReferenceDto>
)
