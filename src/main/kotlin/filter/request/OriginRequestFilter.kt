package filter.request

import com.google.inject.Inject
import com.google.inject.name.Named
import exception.UnauthorizedException
import model.enums.Environment
import mu.KotlinLogging
import org.apache.commons.lang3.StringUtils.isBlank
import java.net.URL
import javax.annotation.Priority
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerRequestFilter

@Priority(700)
class OriginRequestFilter @Inject constructor(
    private val environment: Environment,
    @Named("serverUrl") private val serverUrl: URL
) : ContainerRequestFilter {

    private val logger = KotlinLogging.logger {}

    override fun filter(requestContext: ContainerRequestContext) {
        if (environment == Environment.DEVELOPMENT) {
            logger.debug("OriginRequestFilter ${requestContext.uriInfo.requestUri}")
        }

        var source = requestContext.getHeaderString("Origin")
        if (isBlank(source)) {
            source = requestContext.getHeaderString("Referer")
            if (isBlank(source)) {
                throw UnauthorizedException()
            }
        }

        val sourceURL = URL(source)
        if (this.serverUrl.protocol != sourceURL.protocol ||
            this.serverUrl.host != sourceURL.host ||
            this.serverUrl.port != sourceURL.port
        ) {
            throw UnauthorizedException()
        }
    }
}