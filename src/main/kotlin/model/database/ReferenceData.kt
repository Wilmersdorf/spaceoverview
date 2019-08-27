package model.database

import java.time.LocalDateTime
import java.util.*

data class ReferenceData(
    val id: UUID,
    val spaceId: UUID?,
    val propertyId: UUID?,
    val linkId: UUID?,
    val title: String,
    val url: String?,
    val arxivId: String?,
    val wikipediaId: Int?,
    val bibtex: String?,
    val page: Int?,
    val statement: String?,
    val created: LocalDateTime,
    val updated: LocalDateTime
)