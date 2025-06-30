package model.rest.post

import model.rest.ReferenceDto

data class PostDifferentialEquationPropertyDto(
    val name: String?,
    val description: String?,
    val references: List<ReferenceDto>?
)
