package model.rest

import model.database.PropertyData
import model.enums.FieldLink

data class LinkPropertyDto(
    val field: FieldLink?,
    val linked: Boolean?,
    val computed: Boolean?,
    val property: PropertyData
)
