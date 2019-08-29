package resource

import com.google.inject.Inject
import dao.InviteDao
import dao.UserDao
import model.database.UserData
import model.rest.ErrorDto
import model.rest.post.PostLoginDto
import model.rest.post.PostSignupDto
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.StringUtils.isBlank
import org.apache.commons.validator.routines.EmailValidator
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import service.CookieService
import service.JwtService
import java.time.LocalDateTime
import java.util.*
import javax.ws.rs.Consumes
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status.BAD_REQUEST
import kotlin.collections.HashMap

@Path("/user")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class UserResource @Inject constructor(
    private val userDao: UserDao,
    private val jwtService: JwtService,
    private val cookieService: CookieService,
    private val bCryptPasswordEncoder: BCryptPasswordEncoder,
    private val inviteDao: InviteDao
) {

    private val delay = 500L
    private val setCookieHeader = "Set-Cookie"

    @Path("/logout")
    @POST
    fun logout(): Response {
        val jwtCookie = cookieService.deleteCookie("jwt", true)
        val isAdminCookie = cookieService.deleteCookie("isAdmin", false)
        val canEditCookie = cookieService.deleteCookie("canEdit", false)
        return Response.ok()
            .header(setCookieHeader, jwtCookie)
            .header(setCookieHeader, isAdminCookie)
            .header(setCookieHeader, canEditCookie)
            .build()
    }

    @Path("/signup")
    @POST
    fun signup(postSignupDto: PostSignupDto): Response {
        val errors = validatePostSignupDto(postSignupDto)
        return if (errors.isNotEmpty()) {
            Response.status(BAD_REQUEST).entity(ErrorDto(errors)).build()
        } else {
            Thread.sleep(delay)
            val invite = inviteDao.getByCode(postSignupDto.inviteCode!!)
            if (invite == null) {
                val error = mapOf("inviteCode" to "Please enter a valid invite code.")
                Response.status(BAD_REQUEST).entity(ErrorDto(error)).build()
            } else {
                val hash = bCryptPasswordEncoder.encode(postSignupDto.password)
                val now = LocalDateTime.now()
                val user = UserData(
                    id = UUID.randomUUID(),
                    email = postSignupDto.email!!,
                    hash = hash,
                    isAdmin = false,
                    created = now,
                    updated = now
                )
                try {
                    userDao.create(user)
                } catch (exception: Exception) {
                    val emailError = mapOf("email" to "A user with this email already exists.")
                    Response.status(BAD_REQUEST).entity(ErrorDto(emailError)).build()
                }
                inviteDao.delete(invite.id)
                val jwtToken = jwtService.create(user.id)
                val jwtCookie = cookieService.createCookie("jwt", jwtToken, true)
                val isAdminCookie = cookieService.createCookie("isAdmin", "false", false)
                val canEditCookie = cookieService.createCookie("canEdit", "true", false)
                Response.ok()
                    .header(setCookieHeader, jwtCookie)
                    .header(setCookieHeader, isAdminCookie)
                    .header(setCookieHeader, canEditCookie)
                    .build()
            }
        }
    }

    @Path("/login")
    @POST
    fun login(postLoginDto: PostLoginDto): Response {
        val errors = validatePostLoginDto(postLoginDto)
        return if (errors.isNotEmpty()) {
            Response.status(BAD_REQUEST).entity(ErrorDto(errors)).build()
        } else {
            Thread.sleep(delay)
            val user = userDao.getByEmail(postLoginDto.email!!)
            if (user == null) {
                val error = mapOf("general" to "Email or password invalid.")
                Response.status(BAD_REQUEST).entity(ErrorDto(error)).build()
            } else {
                val valid = bCryptPasswordEncoder.matches(postLoginDto.password, user.hash)
                if (!valid) {
                    val error = mapOf("general" to "Email or password invalid.")
                    Response.status(BAD_REQUEST).entity(ErrorDto(error)).build()
                } else {
                    val jwtToken = jwtService.create(user.id)
                    val jwtCookie = cookieService.createCookie("jwt", jwtToken, true)
                    val isAdminCookie = cookieService.createCookie("isAdmin", user.isAdmin.toString(), false)
                    val canEditCookie = cookieService.createCookie("canEdit", "true", false)
                    Response.ok()
                        .header(setCookieHeader, jwtCookie)
                        .header(setCookieHeader, isAdminCookie)
                        .header(setCookieHeader, canEditCookie)
                        .build()
                }
            }
        }
    }

    private fun validatePostLoginDto(postLoginDto: PostLoginDto): Map<String, String> {
        val errors = HashMap<String, String>()
        if (isBlank(postLoginDto.email)) {
            errors["email"] = "Please enter an email address."
        }
        if (isBlank(postLoginDto.password)) {
            errors["password"] = "Please enter a password"
        }
        return errors
    }

    private fun validatePostSignupDto(postSignupDto: PostSignupDto): Map<String, String> {
        val errors = HashMap<String, String>()
        if (isBlank(postSignupDto.email)) {
            errors["email"] = "Please enter an email address."
        } else if (!EmailValidator.getInstance().isValid(postSignupDto.email)) {
            errors["email"] = "Please enter a valid email address."
        } else if (postSignupDto.email!!.length > 64) {
            errors["email"] = "Please enter an email with at most 64 characters."
        }
        if (isBlank(postSignupDto.password)) {
            errors["password"] = "Please enter a password."
        } else if (StringUtils.contains(postSignupDto.password, " ")) {
            errors["password"] = "Please enter a password without spaces."
        } else if (postSignupDto.password!!.length < 8) {
            errors["password"] = "Please enter a password with at least 8 characters."
        } else if (postSignupDto.password.length > 64) {
            errors["password"] = "Please enter a password with at most 64 characters."
        }
        if (isBlank(postSignupDto.inviteCode)) {
            errors["inviteCode"] = "Please enter an invite code."
        }
        if (!postSignupDto.license) {
            errors["license"] = "You must agree to sign up."
        }
        return errors
    }

}