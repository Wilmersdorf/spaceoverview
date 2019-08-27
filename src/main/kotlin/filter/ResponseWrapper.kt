package filter

import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponseWrapper

class ResponseWrapper(response: HttpServletResponse) : HttpServletResponseWrapper(response) {

    var sc: Int? = null

    override fun sendError(sc: Int) {
        this.sc = sc
    }

}