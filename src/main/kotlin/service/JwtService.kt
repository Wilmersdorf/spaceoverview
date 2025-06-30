package service

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.google.inject.Inject
import util.Logging
import java.time.ZonedDateTime
import java.util.*

class JwtService @Inject constructor(
    private val algorithm: Algorithm,
    private val jwtVerifier: JWTVerifier
) {

    private val logger = Logging.logger {}

    fun create(userId: UUID): String {
        val now = ZonedDateTime.now()
        return JWT.create()
            .withIssuedAt(now.toInstant())
            .withExpiresAt(now.plusMonths(3).toInstant())
            .withClaim("userId", userId.toString())
            .sign(algorithm)
    }

    fun verifyOrNull(token: String): String? {
        return try {
            val jwt = jwtVerifier.verify(token)
            jwt.getClaim("userId").asString()
        } catch (exception: Exception) {
            logger.warn("Unable to verify token.", exception)
            null
        }
    }

}
