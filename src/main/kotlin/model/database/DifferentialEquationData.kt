package model.database

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.ZonedDateTime
import java.util.*

data class DifferentialEquationData(
    val id: UUID,
    val name: String,
    val symbol: String,
    val description: String,
    val variables: String,
    val parameters: String,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSX")
    val created: ZonedDateTime,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSX")
    val updated: ZonedDateTime
)
