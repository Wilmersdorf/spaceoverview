package model.rest

import model.enums.FieldLink
import java.time.LocalDateTime
import java.util.*

data class LinkDto(
    val id: UUID,
    val spaceId: UUID,
    val propertyId: UUID,
    val field: FieldLink,
    val created: LocalDateTime,
    val updated: LocalDateTime,
    val references: List<ReferenceDto>
)