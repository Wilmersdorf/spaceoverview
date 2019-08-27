package service

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.google.inject.Inject
import mu.KotlinLogging
import org.apache.commons.lang3.time.DateUtils
import java.util.*

class JwtService @Inject constructor(
    private val algorithm: Algorithm,
    private val jwtVerifier: JWTVerifier
) {

    private val logger = KotlinLogging.logger {}

    fun create(userId: UUID): String {
        val expiresAt = DateUtils.addMonths(Date(), 3)
        return JWT.create()
            .withIssuedAt(Date())
            .withExpiresAt(expiresAt)
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