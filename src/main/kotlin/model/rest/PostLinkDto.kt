package model.rest

import model.enums.FieldLink

data class PostLinkDto(val field: FieldLink?, val references: List<ReferenceDto>?)