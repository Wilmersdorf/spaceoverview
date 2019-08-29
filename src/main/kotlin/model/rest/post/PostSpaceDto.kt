package model.rest.post

import model.enums.Field
import model.rest.ReferenceDto

data class PostSpaceDto(
    val symbol: String?,
    val norm: String?,
    val description: String?,
    val field: Field,
    val references: List<ReferenceDto>?
)