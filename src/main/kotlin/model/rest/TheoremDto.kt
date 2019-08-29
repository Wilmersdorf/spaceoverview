package model.rest

import java.time.LocalDateTime
import java.util.*

data class TheoremDto(
    val id: UUID,
    val name: String?,
    val created: LocalDateTime,
    val updated: LocalDateTime,
    val conditions: List<ConditionDto>,
    val conclusions: List<ConclusionDto>,
    val references: List<ReferenceDto>
)
