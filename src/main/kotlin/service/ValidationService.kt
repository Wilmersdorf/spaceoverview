package service

import model.rest.ReferenceDto
import org.apache.commons.lang3.StringUtils.isBlank
import org.apache.commons.lang3.StringUtils.isNotBlank
import org.apache.commons.validator.routines.UrlValidator

class ValidationService {

    fun validateIfNotBlank(str: String?, name: String, maxLength: Int): Map<String, String> {
        return if (isBlank(str)) {
            emptyMap()
        } else if (str!!.length > maxLength) {
            mapOf(name to "Please enter a $name with at most $maxLength characters or leave empty.")
        } else {
            emptyMap()
        }
    }

    fun validate(str: String?, name: String, maxLength: Int): Map<String, String> {
        return if (isBlank(str)) {
            mapOf(name to "Please enter a $name.")
        } else if (str!!.length > maxLength) {
            mapOf(name to "Please enter a $name with at most $maxLength characters.")
        } else {
            emptyMap()
        }
    }

    // TODO Improve validation by checking arxiv website, wikipedia website and bibtex
    fun validateReferences(references: List<ReferenceDto>?): Map<String, String> {
        val errors = HashMap<String, String>()
        references.orEmpty().forEachIndexed { index, reference ->
            val key = "reference[$index]"
            val contentCount =
                listOf(
                    isNotBlank(reference.arxivId),
                    reference.wikipediaId != null,
                    isNotBlank(reference.bibtex)
                ).filter { it }.count()
            if (contentCount != 1) {
                errors[key] = "Please provide exactly one of arxiv, wikipedia or bibtex."
            } else if (isBlank(reference.title)) {
                errors[key] = "Title cannot be blank."
            } else if (reference.title!!.length > 128) {
                errors[key] = "Please enter a title with at most 128 characters."
            } else if (reference.page != null && (reference.page < 0 || reference.page > 100000)) {
                errors[key] = "Please enter a valid page number or leave empty."
            } else if (reference.statement != null && reference.statement.length > 128) {
                errors[key] = "Please enter a statement with at most 128 characters or leave empty."
            } else if (reference.url != null &&
                !(UrlValidator.getInstance().isValid(reference.url) ||
                        (reference.url.startsWith("https://en.wikipedia.org/wiki") && UrlValidator.getInstance().isValid(
                            reference.url.replace("–", "%E2%80%93")
                        )))
            ) {
                errors[key] = "Unable to parse url."
            } else if (reference.url != null && reference.url.length > 256) {
                errors[key] = "Unable to parse url."
            } else if (isNotBlank(reference.arxivId) && reference.url == null) {
                errors[key] = "Unable to parse url."
            } else if (reference.wikipediaId != null && reference.url == null) {
                errors[key] = "Unable to parse url."
            } else if (reference.bibtex != null && reference.bibtex.length > 1024) {
                errors[key] = "Please enter bibtex with at most 1024 characters."
            }
        }
        return errors
    }

}