package model

import model.database.*

data class Backup(
    val spaces: List<SpaceData>?,
    val properties: List<PropertyData>?,
    val links: List<LinkData>?,
    val theorems: List<TheoremData>?,
    val conditions: List<ConditionData>?,
    val conclusions: List<ConclusionData>?,
    val references: List<ReferenceData>?
)
