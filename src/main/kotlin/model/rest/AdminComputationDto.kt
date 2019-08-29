package model.rest

import model.database.ComputationData
import model.database.PropertyData
import model.database.SpaceData
import model.database.TheoremData

data class AdminComputationDto(
    val computations: List<ComputationData>,
    val spaces: List<SpaceData>,
    val properties: List<PropertyData>,
    val theorems: List<TheoremData>
)
