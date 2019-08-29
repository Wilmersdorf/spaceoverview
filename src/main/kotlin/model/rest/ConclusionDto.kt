package model.rest

import model.enums.FieldLink
import java.util.*

data class ConclusionDto(val id: UUID, val propertyId: UUID, val propertyName: String, val field: FieldLink)
