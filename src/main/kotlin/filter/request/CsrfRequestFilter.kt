package filter.request

import com.google.inject.Inject
import exception.UnauthorizedException
import jakarta.annotation.Priority
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerRequestFilter
import model.enums.Environment
import org.apache.commons.lang3.StringUtils
import util.Logging

@Priority(800)
class CsrfRequestFilter @Inject constructor(
    private val environment: Environment
) : ContainerRequestFilter {

    private val logger = Logging.logger {}

    override fun filter(requestContext: ContainerRequestContext) {
        if (environment == Environment.DEVELOPMENT) {
            logger.debug("CsrfRequestFilter {}", requestContext.uriInfo.requestUri)
        }

        if (requestContext.method != "GET") {
            val cookie = requestContext.cookies["csrf"]?.value
            val csrfHeader = requestContext.getHeaderString("X-CSRF-TOKEN")
            val contentTypeHeader = requestContext.getHeaderString("Content-Type")
            if (StringUtils.isBlank(cookie) || StringUtils.isBlank(csrfHeader) || cookie != csrfHeader) {
                throw UnauthorizedException()
            } else if (contentTypeHeader != "application/json") {
                throw UnauthorizedException()
            }
        }
    }

}
