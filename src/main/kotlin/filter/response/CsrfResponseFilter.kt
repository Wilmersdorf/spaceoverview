package filter.response

import com.google.inject.Inject
import model.enums.Environment
import mu.KotlinLogging
import service.CookieService
import service.RandomService
import javax.annotation.Priority
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerResponseContext
import javax.ws.rs.container.ContainerResponseFilter

@Priority(900)
class CsrfResponseFilter @Inject constructor(
    private val environment: Environment,
    private val cookieService: CookieService,
    private val randomService: RandomService
) : ContainerResponseFilter {

    private val logger = KotlinLogging.logger {}

    override fun filter(requestContext: ContainerRequestContext, responseContext: ContainerResponseContext) {
        if (environment == Environment.DEVELOPMENT) {
            logger.debug("CsrfResponseFilter ${requestContext.uriInfo.requestUri}")
        }

        val token = randomService.createAlphaNumeric(32)
        val cookie = cookieService.createCookie("csrf", token, false)
        responseContext.headers.add("Set-Cookie", cookie)
    }

}