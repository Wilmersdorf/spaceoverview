package model.rest

import model.database.LinkData
import model.database.PropertyData

data class LinkPropertyDto(val link: LinkData?, val property: PropertyData)