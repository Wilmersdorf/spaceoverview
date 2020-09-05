package model.database

import java.time.LocalDateTime
import java.util.*

data class DifferentialEquationData(
    val id: UUID,
    val name: String,
    val symbol: String,
    val description: String,
    val variables: String,
    val parameters: String,
    val created: LocalDateTime,
    val updated: LocalDateTime
)
