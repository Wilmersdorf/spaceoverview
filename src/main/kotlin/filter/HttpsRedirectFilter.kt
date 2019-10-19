package filter

import com.google.inject.name.Named
import model.enums.Environment
import mu.KotlinLogging
import java.net.URL
import javax.inject.Inject
import javax.servlet.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class HttpsRedirectFilter @Inject constructor(
    @Named("serverUrl") private val serverUrl: URL,
    private val environment: Environment
) : Filter {

    private val logger = KotlinLogging.logger {}

    override fun doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, chain: FilterChain) {
        val request = servletRequest as HttpServletRequest
        if (environment == Environment.DEVELOPMENT) {
            logger.debug("HttpsRedirectFilter ${request.requestURI}")
        }

        val response = servletResponse as HttpServletResponse
        if (request.isSecure) {
            if (serverUrl.host == request.serverName) {
                chain.doFilter(servletRequest, servletResponse)
            } else if (serverUrl.host == "www.${request.serverName}") {
                redirect(request, response)
            } else {
                logger.warn("Unknown request server name.")
                response.sendError(404, "NOT_FOUND")
            }
        } else if (!request.isSecure) {
            if (serverUrl.host != request.serverName && serverUrl.host != "www.${request.serverName}") {
                logger.warn("Unknown request server name.")
                response.sendError(404, "NOT_FOUND")
            } else {
                redirect(request, response)
            }
        } else {
            chain.doFilter(servletRequest, servletResponse)
        }
    }

    private fun redirect(request: HttpServletRequest, response: HttpServletResponse) {
        val port = if (serverUrl.port == -1) {
            ""
        } else {
            "${serverUrl.port}"
        }
        var url = "https://${serverUrl.host}:$port${request.requestURI}"
        if (request.queryString != null) {
            url += "?${request.queryString}"
        }
        response.sendRedirect(url)
    }

    override fun init(filterConfig: FilterConfig?) {}

    override fun destroy() {}

}
