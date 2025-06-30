package resource

import com.google.inject.Inject
import dao.InviteDao
import dao.UserDao
import exception.JsonException
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import model.database.UserData
import model.rest.post.PostLoginDto
import model.rest.post.PostSignupDto
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.StringUtils.isBlank
import org.apache.commons.validator.routines.EmailValidator
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import service.CookieService
import service.JwtService
import java.security.SecureRandom
import java.time.ZonedDateTime
import java.util.*

@Path("/user")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class UserResource @Inject constructor(
    private val bCryptPasswordEncoder: BCryptPasswordEncoder,
    private val cookieService: CookieService,
    private val inviteDao: InviteDao,
    private val jwtService: JwtService,
    private val userDao: UserDao
) {

    private val setCookieHeader = "Set-Cookie"

    private val secureRandom = SecureRandom()

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
        if (errors.isNotEmpty()) {
            throw JsonException(errors)
        } else {
            Thread.sleep(secureRandom.nextInt(100).toLong())
            val invite = inviteDao.getByCode(postSignupDto.inviteCode!!)
            if (invite == null) {
                throw JsonException(
                    key = "inviteCode",
                    value = "Please enter a valid invite code."
                )
            } else {
                val hash = bCryptPasswordEncoder.encode(postSignupDto.password)
                val now = ZonedDateTime.now()
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
                    throw JsonException(
                        key = "email",
                        value = "A user with this email already exists."
                    )
                }
                inviteDao.delete(invite.id)
                val jwtToken = jwtService.create(user.id)
                val jwtCookie = cookieService.createCookie("jwt", jwtToken, true)
                val isAdminCookie = cookieService.createCookie("isAdmin", "false", false)
                val canEditCookie = cookieService.createCookie("canEdit", "true", false)
                return Response.ok()
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
        if (errors.isNotEmpty()) {
            throw JsonException(errors)
        } else {
            Thread.sleep(secureRandom.nextInt(100).toLong())
            val user = userDao.getByEmail(postLoginDto.email!!)
            if (user == null) {
                throw JsonException(
                    key = "general",
                    value = "Email or password invalid."
                )
            } else {
                val valid = bCryptPasswordEncoder.matches(postLoginDto.password, user.hash)
                if (!valid) {
                    throw JsonException(
                        key = "general",
                        value = "Email or password invalid."
                    )
                } else {
                    val jwtToken = jwtService.create(user.id)
                    val jwtCookie = cookieService.createCookie("jwt", jwtToken, true)
                    val isAdminCookie = cookieService.createCookie("isAdmin", user.isAdmin.toString(), false)
                    val canEditCookie = cookieService.createCookie("canEdit", "true", false)
                    return Response.ok()
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
