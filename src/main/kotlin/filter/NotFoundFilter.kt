package filter

import com.google.inject.Inject
import model.enums.Environment
import mu.KotlinLogging
import javax.servlet.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class NotFoundFilter @Inject constructor(private val environment: Environment) : Filter {

    private val logger = KotlinLogging.logger {}

    override fun doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, chain: FilterChain) {
        val request = servletRequest as HttpServletRequest
        if (environment == Environment.DEVELOPMENT) {
            logger.debug("NotFoundFilter ${request.requestURI}")
        }

        val response = servletResponse as HttpServletResponse
        if (request.requestURI == "/" || request.requestURI.startsWith("/api")) {
            chain.doFilter(servletRequest, servletResponse)
        } else {
            val wrapper = ResponseWrapper(response)
            chain.doFilter(servletRequest, wrapper)
            val sc = wrapper.sc
            if (sc == HttpServletResponse.SC_NOT_FOUND) {
                request.getRequestDispatcher("/").forward(request, response)
            } else if (sc != null) {
                response.sendError(sc)
            }
        }
    }

    override fun init(filterConfig: FilterConfig?) {}

    override fun destroy() {}

}