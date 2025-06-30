package model.rest

import model.database.DifferentialEquationPropertyData

data class DifferentialEquationLinkPropertyDto(
    val hasProperty: Boolean?,
    val differentialEquationProperty: DifferentialEquationPropertyData
)
