package model.rest

import model.enums.Field

data class PostSpaceDto(
    val symbol: String?,
    val norm: String?,
    val description: String?,
    val field: Field,
    val references: List<ReferenceDto>?
)