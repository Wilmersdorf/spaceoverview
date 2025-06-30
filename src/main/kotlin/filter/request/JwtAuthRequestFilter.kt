package filter.request

import com.google.inject.Inject
import dao.UserDao
import exception.UnauthorizedException
import jakarta.annotation.Priority
import jakarta.ws.rs.Priorities
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerRequestFilter
import jakarta.ws.rs.core.SecurityContext
import model.User
import model.enums.Environment
import service.JwtService
import util.Logging
import java.security.Principal
import java.util.*

@Priority(Priorities.AUTHENTICATION)
class JwtAuthRequestFilter @Inject constructor(
    private val environment: Environment,
    private val jwtService: JwtService,
    private val userDao: UserDao
) : ContainerRequestFilter {

    private val logger = Logging.logger {}

    override fun filter(requestContext: ContainerRequestContext) {
        if (environment == Environment.DEVELOPMENT) {
            logger.debug("JwtAuthRequestFilter {}", requestContext.uriInfo.requestUri)
        }

        val jwtCookie = requestContext.cookies["jwt"]
        if (jwtCookie != null) {
            val userId = jwtService.verifyOrNull(jwtCookie.value)
            if (userId != null) {
                val userData = userDao.get(UUID.fromString(userId))
                if (userData != null) {
                    val user = User(userData.id, userData.isAdmin)
                    val securityContext = requestContext.securityContext
                    val secure = securityContext != null && securityContext.isSecure
                    requestContext.securityContext = object : SecurityContext {
                        override fun isUserInRole(role: String?): Boolean {
                            return false
                        }

                        override fun getAuthenticationScheme(): String {
                            return "jwt"
                        }

                        override fun getUserPrincipal(): Principal {
                            return user
                        }

                        override fun isSecure(): Boolean {
                            return secure
                        }
                    }
                } else {
                    throw UnauthorizedException()
                }
            } else {
                throw UnauthorizedException()
            }
        } else {
            throw UnauthorizedException()
        }
    }

}
