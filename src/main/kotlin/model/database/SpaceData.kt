package model.database

import com.fasterxml.jackson.annotation.JsonFormat
import model.enums.Field
import java.time.ZonedDateTime
import java.util.*

data class SpaceData(
    val id: UUID,
    val symbol: String,
    val norm: String,
    val description: String,
    val field: Field,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSX")
    val created: ZonedDateTime,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSX")
    val updated: ZonedDateTime
)
