package filter

import com.google.inject.Inject
import model.enums.Environment
import mu.KotlinLogging
import javax.servlet.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class SecurityHeadersFilter @Inject constructor(private val environment: Environment) : Filter {

    private val logger = KotlinLogging.logger {}

    override fun doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, chain: FilterChain) {
        val request = servletRequest as HttpServletRequest
        if (environment == Environment.DEVELOPMENT) {
            logger.debug("SecurityHeadersFilter ${request.requestURI}")
        }

        val response = servletResponse as HttpServletResponse
        response.setHeader("X-XSS-Protection", "1; mode=block")
        response.setHeader("X-Content-Type-Options", "nosniff")
        response.setHeader("X-Frame-Options", "deny")
        response.setHeader("X-Permitted-Cross-Domain-Policies", "none")
        response.setHeader("Referrer-Policy", "same-origin")
        response.setHeader(
            "Content-Security-Policy", "default-src 'none';" +
                    "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net/npm/katex@0.10.2/dist/katex.min.css;" +
                    "img-src 'self';" +
                    "font-src https://cdn.jsdelivr.net/npm/katex@0.10.2/dist/fonts/;" +
                    "script-src 'self' 'unsafe-eval' https://cdn.jsdelivr.net/npm/katex@0.10.2/dist/katex.min.js " +
                    "https://cdn.jsdelivr.net/npm/katex@0.10.2/dist/contrib/auto-render.min.js;" +
                    "connect-src 'self' https://en.wikipedia.org/w/api.php https://export.arxiv.org/api/query"
        )
        response.setHeader(
            "Strict-Transport-Security", "max-age=31536000; includeSubDomains"
        )
        chain.doFilter(servletRequest, servletResponse)
    }

    override fun init(filterConfig: FilterConfig?) {}

    override fun destroy() {}

}