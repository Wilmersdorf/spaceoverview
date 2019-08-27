package model

import model.database.LinkData
import model.database.PropertyData
import model.database.ReferenceData
import model.database.SpaceData

data class Backup(
    val spaces: List<SpaceData>?,
    val properties: List<PropertyData>?,
    val links: List<LinkData>?,
    val references: List<ReferenceData>?
)