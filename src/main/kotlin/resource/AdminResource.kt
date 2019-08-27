package resource

import com.google.inject.Inject
import dao.InviteDao
import exception.UnauthorizedException
import io.dropwizard.auth.Auth
import model.User
import model.database.InviteData
import org.glassfish.jersey.media.multipart.FormDataBodyPart
import org.glassfish.jersey.media.multipart.FormDataParam
import service.BackupService
import service.RandomService
import java.time.LocalDateTime
import java.util.*
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/admin")
class AdminResource @Inject constructor(
    private val backupService: BackupService,
    private val inviteDao: InviteDao,
    private val randomService: RandomService
) {

    @Path("/export")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    fun export(@Auth user: User): Response {
        if (!user.isAdmin) {
            throw UnauthorizedException()
        } else {
            val file = backupService.export()
            val name = LocalDateTime.now().toString() + "-backup.json"
            return Response.ok(file, MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"$name\"")
                .build()
        }
    }

    @Path("/import")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    fun import(@FormDataParam("file") body: FormDataBodyPart, @Auth user: User): Response {
        if (!user.isAdmin) {
            throw UnauthorizedException()
        } else {
            backupService.import(body)
            return Response.ok().build()
        }
    }

    @Path("/invite")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun import(@Auth user: User): Response {
        if (!user.isAdmin) {
            throw UnauthorizedException()
        } else {
            val now = LocalDateTime.now()
            val invite = InviteData(
                id = UUID.randomUUID(),
                code = randomService.createAlphaNumeric(16),
                created = now,
                updated = now
            )
            inviteDao.create(invite)
            return Response.ok(invite).build()
        }
    }

}