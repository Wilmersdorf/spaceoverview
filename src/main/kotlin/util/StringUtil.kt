package util

import org.apache.commons.lang3.StringUtils

fun String?.reduceToNull(): String? {
    return if (StringUtils.isBlank(this)) null else this!!.trim()
}
