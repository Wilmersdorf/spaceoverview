package model.rest.post

import model.rest.ReferenceDto

data class PostDifferentialEquationLinkDto(
    val hasProperty: Boolean?,
    val description: String?,
    val references: List<ReferenceDto>?
)
