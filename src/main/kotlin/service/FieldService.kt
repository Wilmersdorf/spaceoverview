package service

import model.database.ComputationData
import model.database.LinkData
import model.enums.FieldLink
import model.enums.FieldLink.*

class FieldService {

    fun merge(pair: Pair<Boolean?, Boolean?>): FieldLink? {
        return when (pair) {
            Pair(true, null) -> REAL
            Pair(null, true) -> COMPLEX
            Pair(true, true) -> REAL_AND_COMPLEX
            Pair(false, null) -> NOT_REAL
            Pair(null, false) -> NOT_COMPLEX
            Pair(false, false) -> NOT_REAL_AND_NOT_COMPLEX
            Pair(true, false) -> REAL_AND_NOT_COMPLEX
            Pair(false, true) -> NOT_REAL_AND_COMPLEX
            else -> null
        }
    }

    fun real(fieldLink: FieldLink?): Boolean? {
        return if (fieldLink == null) {
            null
        } else {
            split(fieldLink).first
        }
    }

    fun complex(fieldLink: FieldLink?): Boolean? {
        return if (fieldLink == null) {
            null
        } else {
            split(fieldLink).second
        }
    }

    fun getCombinedField(link: LinkData?, computations: List<ComputationData>): FieldLink {
        val realLink = real(link?.field)
        val complexLink = complex(link?.field)
        val realComputations = computations.mapNotNull { real(it.field) }.toSet()
        val realComputation = if (realComputations.size == 1) {
            realComputations.first()
        } else {
            null
        }
        val complexComputations = computations.mapNotNull { complex(it.field) }.toSet()
        val complexComputation = if (complexComputations.size == 1) {
            complexComputations.first()
        } else {
            null
        }
        val realMaybeCombined = setOf(realLink, realComputation).filter { it != null }
        val realCombined = if (realMaybeCombined.size == 1) {
            realMaybeCombined.first()
        } else {
            realLink
        }
        val complexMaybeCombined = setOf(complexLink, complexComputation).filter { it != null }
        val complexCombined = if (complexMaybeCombined.size == 1) {
            complexMaybeCombined.first()
        } else {
            complexLink
        }
        return merge(Pair(realCombined, complexCombined))!!
    }

    private fun split(fieldLink: FieldLink): Pair<Boolean?, Boolean?> {
        return when (fieldLink) {
            REAL -> Pair(true, null)
            COMPLEX -> Pair(null, true)
            REAL_AND_COMPLEX -> Pair(true, true)
            NOT_REAL -> Pair(false, null)
            NOT_COMPLEX -> Pair(null, false)
            NOT_REAL_AND_NOT_COMPLEX -> Pair(false, false)
            REAL_AND_NOT_COMPLEX -> Pair(true, false)
            NOT_REAL_AND_COMPLEX -> Pair(false, true)
        }
    }

}
