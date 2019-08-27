package service

import java.security.SecureRandom

class RandomService {

    private val characters: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    private val secureRandom = SecureRandom()

    fun createAlphaNumeric(length: Int): String {
        return (1..length).map { secureRandom.nextInt(characters.size) }
            .map(characters::get)
            .joinToString(separator = "")
    }

}