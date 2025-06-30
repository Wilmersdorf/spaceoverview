package model.database

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.ZonedDateTime
import java.util.*

data class InviteData(
    val id: UUID,
    val code: String,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSX")
    val created: ZonedDateTime,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSX")
    val updated: ZonedDateTime
)
