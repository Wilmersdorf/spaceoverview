package model.database

import model.enums.FieldLink
import java.time.LocalDateTime
import java.util.*

data class ComputationData(
    val id: UUID,
    val spaceId: UUID,
    val propertyId: UUID,
    val theoremId: UUID,
    val field: FieldLink,
    val created: LocalDateTime,
    val updated: LocalDateTime
)
