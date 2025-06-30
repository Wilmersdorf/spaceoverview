package model.rest

import model.enums.FieldLink
import java.time.ZonedDateTime
import java.util.*

data class LinkDto(
    val id: UUID,
    val spaceId: UUID,
    val propertyId: UUID,
    val field: FieldLink,
    val description: String?,
    val created: ZonedDateTime,
    val updated: ZonedDateTime,
    val references: List<ReferenceDto>
)
