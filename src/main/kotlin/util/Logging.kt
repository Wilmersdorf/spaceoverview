package util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Logging {

    fun logger(func: () -> Unit): Logger {
        val name = func.javaClass.name
        return LoggerFactory.getLogger(name.substringBefore("$"))
    }

}
