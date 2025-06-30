package mapper

import model.database.*
import model.rest.*
import java.util.*

class ToDtoMapper {

    fun toSpaceDto(space: SpaceData, references: List<ReferenceData>): SpaceDto {
        return SpaceDto(
            id = space.id,
            symbol = space.symbol,
            norm = space.norm,
            description = space.description,
            field = space.field,
            created = space.created,
            updated = space.updated,
            references = toReferenceDtoList(references)
        )
    }

    fun toPropertyDto(property: PropertyData, references: List<ReferenceData>): PropertyDto {
        return PropertyDto(
            id = property.id,
            name = property.name,
            description = property.description,
            field = property.field,
            created = property.created,
            updated = property.updated,
            references = toReferenceDtoList(references)
        )
    }

    fun toLinkDto(link: LinkData, references: List<ReferenceData>): LinkDto {
        return LinkDto(
            id = link.id,
            spaceId = link.spaceId,
            propertyId = link.propertyId,
            field = link.field,
            description = link.description,
            created = link.created,
            updated = link.created,
            references = toReferenceDtoList(references)
        )
    }

    fun toDifferentialEquationLinkDto(
        link: DifferentialEquationLinkData,
        references: List<ReferenceData>
    ): DifferentialEquationLinkDto {
        return DifferentialEquationLinkDto(
            id = link.id,
            differentialEquationId = link.differentialEquationId,
            differentialEquationPropertyId = link.differentialEquationPropertyId,
            hasProperty = link.hasProperty,
            description = link.description,
            created = link.created,
            updated = link.created,
            references = toReferenceDtoList(references)
        )
    }

    fun toTheoremDto(
        theorem: TheoremData,
        conditions: List<ConditionData>,
        conclusions: List<ConclusionData>,
        references: List<ReferenceData>,
        properties: Map<UUID, PropertyData>
    ): TheoremDto {
        val conditionDtoList = conditions.map {
            ConditionDto(
                id = it.id,
                propertyId = it.propertyId,
                propertyName = properties[it.propertyId]?.name.orEmpty(),
                field = it.field
            )
        }
        val conclusionDtoList = conclusions.map {
            ConclusionDto(
                id = it.id,
                propertyId = it.propertyId,
                propertyName = properties[it.propertyId]?.name.orEmpty(),
                field = it.field
            )
        }
        return TheoremDto(
            id = theorem.id,
            name = theorem.name,
            description = theorem.description,
            created = theorem.created,
            updated = theorem.updated,
            conditions = conditionDtoList,
            conclusions = conclusionDtoList,
            references = toReferenceDtoList(references)
        )
    }

    fun toDifferentialEquationDto(
        differentialEquation: DifferentialEquationData,
        references: List<ReferenceData>
    ): DifferentialEquationDto {
        return DifferentialEquationDto(
            id = differentialEquation.id,
            name = differentialEquation.name,
            symbol = differentialEquation.symbol,
            description = differentialEquation.description,
            variables = differentialEquation.variables,
            parameters = differentialEquation.parameters,
            created = differentialEquation.created,
            updated = differentialEquation.updated,
            references = toReferenceDtoList(references)
        )
    }

    fun toDifferentialEquationPropertyDto(
        differentialEquationPropertyData: DifferentialEquationPropertyData,
        references: List<ReferenceData>
    ): DifferentialEquationPropertyDto {
        return DifferentialEquationPropertyDto(
            id = differentialEquationPropertyData.id,
            name = differentialEquationPropertyData.name,
            description = differentialEquationPropertyData.description,
            created = differentialEquationPropertyData.created,
            updated = differentialEquationPropertyData.updated,
            references = toReferenceDtoList(references)
        )
    }

    private fun toReferenceDtoList(references: List<ReferenceData>): List<ReferenceDto> {
        return references.map(this::toReferenceDto)
    }

    private fun toReferenceDto(reference: ReferenceData): ReferenceDto {
        return ReferenceDto(
            url = reference.url,
            title = reference.title,
            arxivId = reference.arxivId,
            wikipediaId = reference.wikipediaId,
            bibtex = reference.bibtex,
            page = reference.page,
            statement = reference.statement
        )
    }

}
