package exception

import model.rest.ErrorDto
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.Response

class JsonException(errors: Map<String, String>) :
    WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(ErrorDto(errors)).build()) {

    constructor(key: String, value: String) : this(mapOf(key to value))

}
