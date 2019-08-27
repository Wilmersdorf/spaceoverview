package model

import java.security.Principal
import java.util.*

data class User(val id: UUID, val isAdmin: Boolean) : Principal {

    override fun getName(): String {
        return id.toString()
    }

}