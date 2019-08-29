package model.rest.post

import model.enums.FieldLink
import java.util.*

data class PostConclusionDto(
    val propertyId: UUID,
    val field: FieldLink?
)
