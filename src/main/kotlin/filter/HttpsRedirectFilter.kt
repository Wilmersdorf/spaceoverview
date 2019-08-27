package filter

import com.google.inject.name.Named
import model.enums.Environment
import mu.KotlinLogging
import java.net.URL
import java.util.regex.Pattern
import javax.inject.Inject
import javax.servlet.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class HttpsRedirectFilter @Inject constructor(
    @Named("serverUrl") private val serverUrl: URL,
    private val environment: Environment
) : Filter {

    private val logger = KotlinLogging.logger {}

    private val newLineCharacters = Pattern.compile("[\\r\\n]")

    override fun doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, chain: FilterChain) {
        val request = servletRequest as HttpServletRequest
        if (environment == Environment.DEVELOPMENT) {
            logger.debug("HttpsRedirectFilter ${request.requestURI}")
        }

        val response = servletResponse as HttpServletResponse
        if (!request.isSecure) {
            if (serverUrl.host != request.serverName && serverUrl.host != "www.${request.serverName}") {
                logger.warn("Unknown request server name.")
                response.sendError(404)
                return
            } else {
                val port = if (serverUrl.port == -1) {
                    ""
                } else {
                    ":${serverUrl.port}"
                }
                var url = "https://${serverUrl.host}$port${request.requestURI}"
                if (request.queryString != null) {
                    url += "?${request.queryString}"
                }
                if (newLineCharacters.matcher(url).find()) {
                    logger.warn("Attempted response split attack.")
                    response.sendError(400)
                    return
                } else {
                    response.sendRedirect(url)
                    return
                }
            }
        } else {
            chain.doFilter(servletRequest, servletResponse)
        }
    }

    override fun init(filterConfig: FilterConfig?) {}

    override fun destroy() {}

}