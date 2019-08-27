package model.rest

data class ReferenceDto(
    val title: String?,
    val url: String?,
    val arxivId: String?,
    val wikipediaId: Int?,
    val bibtex: String?,
    val page: Int?,
    val statement: String?
)