package exception

import jakarta.ws.rs.WebApplicationException
import jakarta.ws.rs.core.Response
import model.rest.ErrorDto

class JsonException(errors: Map<String, String>) :
    WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity(ErrorDto(errors)).build()) {

    constructor(key: String, value: String) : this(mapOf(key to value))

}
