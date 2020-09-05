package model.rest.post

import model.rest.ReferenceDto

data class PostDifferentialEquationDto(
    val name: String?,
    val symbol: String?,
    val description: String?,
    val variables: String?,
    val parameters: String?,
    val references: List<ReferenceDto>?
)
