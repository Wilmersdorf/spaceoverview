package model.rest

import model.database.DifferentialEquationData

data class DifferentialEquationLinkDifferentialEquationDto(
    val hasProperty: Boolean,
    val differentialEquation: DifferentialEquationData
)
