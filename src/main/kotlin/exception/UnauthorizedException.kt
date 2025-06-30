package exception

import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.Response

class UnauthorizedException :
    WebApplicationException("Credentials are required to access this resource.", Response.Status.UNAUTHORIZED)
