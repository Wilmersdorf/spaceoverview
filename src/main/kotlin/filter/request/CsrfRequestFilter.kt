package filter.request

import com.google.inject.Inject
import exception.UnauthorizedException
import model.enums.Environment
import mu.KotlinLogging
import org.apache.commons.lang3.StringUtils
import javax.annotation.Priority
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerRequestFilter

@Priority(800)
class CsrfRequestFilter @Inject constructor(
    private val environment: Environment
) : ContainerRequestFilter {

    private val logger = KotlinLogging.logger {}

    override fun filter(requestContext: ContainerRequestContext) {
        if (environment == Environment.DEVELOPMENT) {
            logger.debug("CsrfRequestFilter ${requestContext.uriInfo.requestUri}")
        }

        if (requestContext.method != "GET") {
            val cookie = requestContext.cookies["csrf"]?.value
            val header = requestContext.getHeaderString("X-CSRF-TOKEN")
            if (StringUtils.isBlank(cookie) || StringUtils.isBlank(header) || cookie != header) {
                throw UnauthorizedException()
            }
        }
    }

}