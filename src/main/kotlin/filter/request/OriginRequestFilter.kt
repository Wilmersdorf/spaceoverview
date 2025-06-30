package filter.request

import com.google.inject.Inject
import com.google.inject.name.Named
import exception.UnauthorizedException
import jakarta.annotation.Priority
import jakarta.ws.rs.container.ContainerRequestContext
import jakarta.ws.rs.container.ContainerRequestFilter
import model.enums.Environment
import org.apache.commons.lang3.StringUtils.isBlank
import util.Logging
import java.net.URL

@Priority(700)
class OriginRequestFilter @Inject constructor(
    @Named("serverUrl") private val serverUrl: URL,
    private val environment: Environment
) : ContainerRequestFilter {

    private val logger = Logging.logger {}

    override fun filter(requestContext: ContainerRequestContext) {
        if (environment == Environment.DEVELOPMENT) {
            logger.debug("OriginRequestFilter {}", requestContext.uriInfo.requestUri)
        }

        var source = requestContext.getHeaderString("Origin")
        if (isBlank(source)) {
            source = requestContext.getHeaderString("Referer")
            if (isBlank(source)) {
                throw UnauthorizedException()
            }
        }
        val sourceURL = try {
            URL(source)
        } catch (exception: Exception) {
            throw UnauthorizedException()
        }
        if (serverUrl.protocol != sourceURL.protocol ||
            serverUrl.host != sourceURL.host ||
            serverUrl.port != sourceURL.port
        ) {
            throw UnauthorizedException()
        }
    }

}
