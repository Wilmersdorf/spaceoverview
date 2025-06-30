package mapper

import model.database.ConclusionData
import model.database.ConditionData
import model.database.ReferenceData
import model.rest.ReferenceDto
import model.rest.post.PostConclusionDto
import model.rest.post.PostConditionDto
import util.reduceToNull
import java.time.ZonedDateTime
import java.util.*

class ToDataMapper {

    fun toConditions(
        theoremId: UUID,
        time: ZonedDateTime,
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
        time: ZonedDateTime,
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

    fun toSpaceReferences(spaceId: UUID, time: ZonedDateTime, references: List<ReferenceDto>?): List<ReferenceData> {
        return toReferences(spaceId = spaceId, time = time, references = references)
    }

    fun toPropertyReferences(
        propertyId: UUID,
        time: ZonedDateTime,
        references: List<ReferenceDto>?
    ): List<ReferenceData> {
        return toReferences(propertyId = propertyId, time = time, references = references)
    }

    fun toLinkReferences(linkId: UUID, time: ZonedDateTime, references: List<ReferenceDto>?): List<ReferenceData> {
        return toReferences(linkId = linkId, time = time, references = references)
    }

    fun toTheoremReferences(
        theoremId: UUID,
        time: ZonedDateTime,
        references: List<ReferenceDto>?
    ): List<ReferenceData> {
        return toReferences(theoremId = theoremId, time = time, references = references)
    }

    fun toDifferentialEquationReferences(
        differentialEquationId: UUID,
        time: ZonedDateTime,
        references: List<ReferenceDto>?
    ): List<ReferenceData> {
        return toReferences(differentialEquationId = differentialEquationId, time = time, references = references)
    }

    fun toDifferentialEquationPropertyReferences(
        differentialEquationPropertyId: UUID,
        time: ZonedDateTime,
        references: List<ReferenceDto>?
    ): List<ReferenceData> {
        return toReferences(
            differentialEquationPropertyId = differentialEquationPropertyId,
            time = time,
            references = references
        )
    }

    fun toDifferentialEquationLinkReferences(
        differentialEquationLinkId: UUID,
        time: ZonedDateTime,
        references: List<ReferenceDto>?
    ): List<ReferenceData> {
        return toReferences(
            differentialEquationLinkId = differentialEquationLinkId,
            time = time,
            references = references
        )
    }

    private fun toReferences(
        spaceId: UUID? = null,
        propertyId: UUID? = null,
        linkId: UUID? = null,
        theoremId: UUID? = null,
        differentialEquationId: UUID? = null,
        differentialEquationPropertyId: UUID? = null,
        differentialEquationLinkId: UUID? = null,
        time: ZonedDateTime,
        references: List<ReferenceDto>?
    ): List<ReferenceData> {
        return references.orEmpty().map {
            ReferenceData(
                id = UUID.randomUUID(),
                spaceId = spaceId,
                propertyId = propertyId,
                linkId = linkId,
                theoremId = theoremId,
                differentialEquationId = differentialEquationId,
                differentialEquationPropertyId = differentialEquationPropertyId,
                differentialEquationLinkId = differentialEquationLinkId,
                url = it.url.reduceToNull(),
                title = it.title.reduceToNull()!!,
                arxivId = it.arxivId.reduceToNull(),
                wikipediaId = it.wikipediaId,
                bibtex = it.bibtex.reduceToNull(),
                page = it.page,
                statement = it.statement.reduceToNull(),
                created = time,
                updated = time
            )
        }
    }

}
