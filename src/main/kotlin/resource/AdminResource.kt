package resource

import com.google.inject.Inject
import dao.InviteDao
import dao.PropertyDao
import dao.SpaceDao
import dao.TheoremDao
import exception.UnauthorizedException
import io.dropwizard.auth.Auth
import model.User
import model.database.InviteData
import model.rest.AdminComputationDto
import org.glassfish.jersey.media.multipart.FormDataBodyPart
import org.glassfish.jersey.media.multipart.FormDataParam
import service.BackupService
import service.ComputationService
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
    private val randomService: RandomService,
    private val computationService: ComputationService,
    private val theoremDao: TheoremDao,
    private val propertyDao: PropertyDao,
    private val spaceDao: SpaceDao
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
            computationService.compute()
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

    @Path("/compute")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun compute(@Auth user: User): Response {
        if (!user.isAdmin) {
            throw UnauthorizedException()
        } else {
            val computations = computationService.compute()
            val theorems = theoremDao.getAll()
            val properties = propertyDao.getAll()
            val spaces = spaceDao.getAll()
            val adminComputationDto = AdminComputationDto(
                computations = computations,
                spaces = spaces,
                properties = properties,
                theorems = theorems
            )
            return Response.ok(adminComputationDto).build()
        }
    }

}
