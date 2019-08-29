package mapper

import model.database.ConclusionData
import model.database.ConditionData
import model.database.ReferenceData
import model.rest.ReferenceDto
import model.rest.post.PostConclusionDto
import model.rest.post.PostConditionDto
import java.time.LocalDateTime
import java.util.*

class ToDataMapper {

    fun toConditions(
        theoremId: UUID,
        time: LocalDateTime,
        postConditionDtoList: List<PostConditionDto>
    ): List<ConditionData> {
        return postConditionDtoList.map {
            ConditionData(
                id = UUID.randomUUID(),
                theoremId = theoremId,
                propertyId = it.propertyId,
                field = it.field!!,
                created = time,
                updated = time
            )
        }.toList()
    }

    fun toConclusions(
        theoremId: UUID,
        time: LocalDateTime,
        postConclusionDtoList: List<PostConclusionDto>
    ): List<ConclusionData> {
        return postConclusionDtoList.map {
            ConclusionData(
                id = UUID.randomUUID(),
                theoremId = theoremId,
                propertyId = it.propertyId,
                field = it.field!!,
                created = time,
                updated = time
            )
        }.toList()
    }

    fun toSpaceReferences(spaceId: UUID, time: LocalDateTime, references: List<ReferenceDto>?): List<ReferenceData> {
        return toReferences(spaceId = spaceId, time = time, references = references)
    }

    fun toPropertyReferences(
        propertyId: UUID,
        time: LocalDateTime,
        references: List<ReferenceDto>?
    ): List<ReferenceData> {
        return toReferences(propertyId = propertyId, time = time, references = references)
    }

    fun toLinkReferences(linkId: UUID, time: LocalDateTime, references: List<ReferenceDto>?): List<ReferenceData> {
        return toReferences(linkId = linkId, time = time, references = references)
    }

    fun toTheoremReferences(
        theoremId: UUID,
        time: LocalDateTime,
        references: List<ReferenceDto>?
    ): List<ReferenceData> {
        return toReferences(theoremId = theoremId, time = time, references = references)
    }

    private fun toReferences(
        spaceId: UUID? = null,
        propertyId: UUID? = null,
        linkId: UUID? = null,
        theoremId: UUID? = null,
        time: LocalDateTime,
        references: List<ReferenceDto>?
    ): List<ReferenceData> {
        return references.orEmpty().map {
            ReferenceData(
                id = UUID.randomUUID(),
                spaceId = spaceId,
                propertyId = propertyId,
                linkId = linkId,
                theoremId = theoremId,
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