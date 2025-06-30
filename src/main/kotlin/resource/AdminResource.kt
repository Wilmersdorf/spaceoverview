package resource

import com.google.inject.Inject
import dao.InviteDao
import dao.PropertyDao
import dao.SpaceDao
import dao.TheoremDao
import exception.UnauthorizedException
import io.dropwizard.auth.Auth
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import model.User
import model.database.InviteData
import model.rest.AdminComputationDto
import model.rest.post.PostBackupDto
import service.BackupService
import service.ComputationService
import service.RandomService
import java.time.ZonedDateTime
import java.util.*

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Path("/admin")
class AdminResource @Inject constructor(
    private val backupService: BackupService,
    private val computationService: ComputationService,
    private val inviteDao: InviteDao,
    private val propertyDao: PropertyDao,
    private val randomService: RandomService,
    private val spaceDao: SpaceDao,
    private val theoremDao: TheoremDao
) {

    @Path("/export")
    @GET
    fun export(@Auth user: User): Response {
        if (!user.isAdmin) {
            throw UnauthorizedException()
        } else {
            val dto = backupService.export()
            return Response.ok(dto).build()
        }
    }

    @Path("/import")
    @POST
    fun import(@Auth user: User, backup: PostBackupDto): Response {
        if (!user.isAdmin) {
            throw UnauthorizedException()
        } else {
            backupService.import(backup)
            computationService.compute()
            return Response.ok().build()
        }
    }

    @Path("/invite")
    @POST
    fun invite(@Auth user: User): Response {
        if (!user.isAdmin) {
            throw UnauthorizedException()
        } else {
            val now = ZonedDateTime.now()
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
