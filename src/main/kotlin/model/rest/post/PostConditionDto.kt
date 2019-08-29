package model.rest.post

import model.enums.FieldLink
import java.util.*

data class PostConditionDto(
    val propertyId: UUID,
    val field: FieldLink?
)
