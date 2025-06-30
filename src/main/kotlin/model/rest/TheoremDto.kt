package model.rest

import java.time.ZonedDateTime
import java.util.*

data class TheoremDto(
    val id: UUID,
    val name: String?,
    val description: String?,
    val created: ZonedDateTime,
    val updated: ZonedDateTime,
    val conditions: List<ConditionDto>,
    val conclusions: List<ConclusionDto>,
    val references: List<ReferenceDto>
)
