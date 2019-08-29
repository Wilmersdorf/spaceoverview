package model.rest.post

import model.enums.Field
import model.rest.ReferenceDto

data class PostPropertyDto(
    val name: String?,
    val description: String?,
    val field: Field,
    val references: List<ReferenceDto>?
)