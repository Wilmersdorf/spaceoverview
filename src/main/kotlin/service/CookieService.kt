package service

import jakarta.ws.rs.core.NewCookie

class CookieService {

    private val maxAge = 3 * 28 * 24 * 60 * 60
    private val lax = ";SameSite=lax"

    fun createCookie(name: String, value: String, httpOnly: Boolean): String {
        val cookie = NewCookie(name, value, "/", null, null, maxAge, true, httpOnly)
        return "$cookie$lax"
    }

    fun deleteCookie(name: String, httpOnly: Boolean): NewCookie {
        return NewCookie(name, "", "/", null, null, 0, true, httpOnly)
    }

}
