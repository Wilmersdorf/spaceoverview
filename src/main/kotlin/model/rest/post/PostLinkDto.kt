package model.rest.post

import model.enums.FieldLink
import model.rest.ReferenceDto

data class PostLinkDto(val field: FieldLink?, val references: List<ReferenceDto>?)
