package model.database

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.ZonedDateTime
import java.util.*

data class ReferenceData(
    val id: UUID,
    val spaceId: UUID?,
    val propertyId: UUID?,
    val linkId: UUID?,
    val theoremId: UUID?,
    val differentialEquationId: UUID?,
    val differentialEquationPropertyId: UUID?,
    val differentialEquationLinkId: UUID?,
    val title: String,
    val url: String?,
    val arxivId: String?,
    val wikipediaId: Int?,
    val bibtex: String?,
    val page: Int?,
    val statement: String?,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSX")
    val created: ZonedDateTime,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSX")
    val updated: ZonedDateTime
)
