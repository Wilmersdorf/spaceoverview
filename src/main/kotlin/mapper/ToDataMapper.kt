package mapper

import model.database.ReferenceData
import model.rest.ReferenceDto
import java.time.LocalDateTime
import java.util.*

class ToDataMapper {

    fun toSpaceReferences(spaceId: UUID, time: LocalDateTime, references: List<ReferenceDto>?): List<ReferenceData> {
        return toReferences(spaceId = spaceId, propertyId = null, linkId = null, time = time, references = references)
    }

    fun toPropertyReferences(
        propertyId: UUID,
        time: LocalDateTime,
        references: List<ReferenceDto>?
    ): List<ReferenceData> {
        return toReferences(
            spaceId = null,
            propertyId = propertyId,
            linkId = null,
            time = time,
            references = references
        )
    }

    fun toLinkReferences(linkId: UUID, time: LocalDateTime, references: List<ReferenceDto>?): List<ReferenceData> {
        return toReferences(
            spaceId = null,
            propertyId = null,
            linkId = linkId,
            time = time,
            references = references
        )
    }

    private fun toReferences(
        spaceId: UUID?,
        propertyId: UUID?,
        linkId: UUID?,
        time: LocalDateTime,
        references: List<ReferenceDto>?
    ): List<ReferenceData> {
        return references.orEmpty().map {
            ReferenceData(
                id = UUID.randomUUID(),
                spaceId = spaceId,
                propertyId = propertyId,
                linkId = linkId,
                url = it.url?.trim(),
                title = it.title!!.trim(),
                arxivId = it.arxivId?.trim(),
                wikipediaId = it.wikipediaId,
                bibtex = it.bibtex?.trim(),
                page = it.page,
                statement = it.statement?.trim(),
                created = time,
                updated = time
            )
        }
    }

}