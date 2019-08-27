package model.rest

import model.enums.Field

data class PostPropertyDto(
    val name: String?,
    val description: String?,
    val field: Field,
    val references: List<ReferenceDto>?
)