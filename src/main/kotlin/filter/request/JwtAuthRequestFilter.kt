package filter.request

import com.google.inject.Inject
import dao.UserDao
import exception.UnauthorizedException
import model.User
import model.enums.Environment
import mu.KotlinLogging
import service.JwtService
import java.security.Principal
import java.util.*
import javax.annotation.Priority
import javax.ws.rs.Priorities
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerRequestFilter
import javax.ws.rs.core.SecurityContext

@Priority(Priorities.AUTHENTICATION)
class JwtAuthRequestFilter @Inject constructor(
    private val environment: Environment,
    private val userDao: UserDao,
    private val jwtService: JwtService
) :
    ContainerRequestFilter {

    private val logger = KotlinLogging.logger {}

    override fun filter(requestContext: ContainerRequestContext) {
        if (environment == Environment.DEVELOPMENT) {
            logger.debug("JwtAuthRequestFilter ${requestContext.uriInfo.requestUri}")
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
                    if (environment != Environment.DEVELOPMENT && !secure) {
                        throw UnauthorizedException()
                    } else {
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
