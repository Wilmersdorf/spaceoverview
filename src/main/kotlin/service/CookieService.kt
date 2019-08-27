package service

import model.enums.Environment
import javax.inject.Inject
import javax.ws.rs.core.NewCookie

class CookieService @Inject constructor(environment: Environment) {

    private val secure = environment != Environment.DEVELOPMENT
    private val maxAge = 3 * 28 * 24 * 60 * 60
    private val lax = ";SameSite=lax"

    fun createCookie(name: String, value: String, httpOnly: Boolean): String {
        val cookie = NewCookie(name, value, "/", null, null, maxAge, secure, httpOnly)
        return "$cookie$lax"
    }

    fun deleteCookie(name: String, httpOnly: Boolean): NewCookie {
        return NewCookie(name, "", "/", null, null, 0, secure, httpOnly)
    }

}