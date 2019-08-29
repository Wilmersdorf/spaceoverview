package model.rest.post

import model.rest.ReferenceDto

data class PostTheoremDto(
    val name: String?,
    val conditions: List<PostConditionDto>,
    val conclusions: List<PostConclusionDto>,
    val references: List<ReferenceDto>?
)
