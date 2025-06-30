package filter.response

import com.google.inject.Inject
import jakarta.annotation.Priority
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerResponseContext
import jakarta.ws.rs.container.ContainerResponseFilter
import model.enums.Environment
import service.CookieService
import service.RandomService
import util.Logging

@Priority(900)
class CsrfResponseFilter @Inject constructor(
    private val cookieService: CookieService,
    private val environment: Environment,
    private val randomService: RandomService
) : ContainerResponseFilter {

    private val logger = Logging.logger {}

    override fun filter(requestContext: ContainerRequestContext, responseContext: ContainerResponseContext) {
        if (environment == Environment.DEVELOPMENT) {
            logger.debug("CsrfResponseFilter {}", requestContext.uriInfo.requestUri)
        }

        val token = randomService.createAlphaNumeric(32)
        val cookie = cookieService.createCookie("csrf", token, false)
        responseContext.headers.add("Set-Cookie", cookie)
    }

}
