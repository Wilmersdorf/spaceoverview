package model.rest

import model.database.SpaceData
import model.enums.FieldLink

data class LinkSpaceDto(val field: FieldLink, val linked: Boolean, val computed: Boolean, val space: SpaceData)
