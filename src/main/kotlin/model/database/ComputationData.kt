package model.database

import com.fasterxml.jackson.annotation.JsonFormat
import model.enums.FieldLink
import java.time.ZonedDateTime
import java.util.*

data class ComputationData(
    val id: UUID,
    val spaceId: UUID,
    val propertyId: UUID,
    val theoremId: UUID,
    val field: FieldLink,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSX")
    val created: ZonedDateTime,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSX")
    val updated: ZonedDateTime
)
