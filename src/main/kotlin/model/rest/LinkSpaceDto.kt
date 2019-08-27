package model.rest

import model.database.LinkData
import model.database.SpaceData

data class LinkSpaceDto(val link: LinkData, val space: SpaceData)