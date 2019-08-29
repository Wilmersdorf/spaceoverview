package service

import model.database.ComputationData
import model.database.LinkData
import model.enums.FieldLink

class FieldService {

    private val realComplexMap = mapOf(
        Pair(first = true, second = true) to FieldLink.REAL_AND_COMPLEX,
        Pair(first = true, second = false) to FieldLink.REAL_AND_NOT_COMPLEX,
        Pair(first = true, second = null) to FieldLink.REAL,
        Pair(first = false, second = true) to FieldLink.NOT_REAL_AND_COMPLEX,
        Pair(first = false, second = false) to FieldLink.NOT_REAL_AND_NOT_COMPLEX,
        Pair(first = false, second = null) to FieldLink.NOT_REAL,
        Pair(first = null, second = true) to FieldLink.COMPLEX,
        Pair(first = null, second = false) to FieldLink.NOT_COMPLEX
    )

    fun getCombinedField(link: LinkData?, computations: List<ComputationData>): FieldLink {
        val fields = getFields(link, computations)
        val isReal = isReal(fields)
        val isComplex = isComplex(fields)
        return realComplexMap[Pair(isReal, isComplex)]!!
    }

    private fun getFields(link: LinkData?, computations: List<ComputationData>): Set<FieldLink> {
        val fields = computations.map { it.field }.toMutableSet()
        if (link != null) {
            fields.add(link.field)
        }
        return fields.toSet()
    }

    private fun isReal(fields: Set<FieldLink>): Boolean? {
        if (setOf(
                FieldLink.REAL,
                FieldLink.REAL_AND_COMPLEX,
                FieldLink.REAL_AND_NOT_COMPLEX
            ).intersect(fields).isNotEmpty()
        ) {
            return true
        } else if (setOf(
                FieldLink.NOT_REAL,
                FieldLink.NOT_REAL_AND_NOT_COMPLEX,
                FieldLink.NOT_REAL_AND_COMPLEX
            ).intersect(fields).isNotEmpty()
        ) {
            return false
        } else {
            return null
        }
    }

    private fun isComplex(fields: Set<FieldLink>): Boolean? {
        if (setOf(
                FieldLink.COMPLEX,
                FieldLink.REAL_AND_COMPLEX,
                FieldLink.NOT_REAL_AND_COMPLEX
            ).intersect(fields).isNotEmpty()
        ) {
            return true
        } else if (setOf(
                FieldLink.NOT_COMPLEX,
                FieldLink.NOT_REAL_AND_NOT_COMPLEX,
                FieldLink.REAL_AND_NOT_COMPLEX
            ).intersect(fields).isNotEmpty()
        ) {
            return false
        } else {
            return null
        }
    }

}
