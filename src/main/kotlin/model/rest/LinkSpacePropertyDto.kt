package model.rest

import model.database.ComputationData
import model.enums.FieldLink

data class LinkSpacePropertyDto(
    val space: SpaceDto,
    val field: FieldLink,
    val property: PropertyDto,
    val link: LinkDto?,
    val computations: List<ComputationData>
)
