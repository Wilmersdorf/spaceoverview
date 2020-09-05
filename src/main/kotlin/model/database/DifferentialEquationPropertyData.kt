package model.database

import java.time.LocalDateTime
import java.util.*

data class DifferentialEquationPropertyData(
    val id: UUID,
    val name: String,
    val description: String,
    val created: LocalDateTime,
    val updated: LocalDateTime
)
