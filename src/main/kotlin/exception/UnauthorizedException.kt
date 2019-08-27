package exception

import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.Response

class UnauthorizedException :
    WebApplicationException("Credentials are required to access this resource.", Response.Status.UNAUTHORIZED)