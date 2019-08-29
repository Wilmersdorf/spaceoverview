package model.database

import model.enums.FieldLink
import java.time.LocalDateTime
import java.util.*

data class ConclusionData(
    val id: UUID,
    val theoremId: UUID,
    val propertyId: UUID,
    val field: FieldLink,
    val created: LocalDateTime,
    val updated: LocalDateTime
)
