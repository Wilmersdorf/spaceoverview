package config

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.google.inject.AbstractModule
import com.google.inject.name.Names
import model.enums.Environment
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import service.ComputationService
import java.net.URL

class ApplicationModule(private val configuration: SpaceConfiguration) : AbstractModule() {

    override fun configure() {
        bind(Environment::class.java).toInstance(configuration.environment)
        val algorithm = Algorithm.HMAC256(configuration.jwtSecret)
        bind(Algorithm::class.java).toInstance(algorithm)
        bind(JWTVerifier::class.java).toInstance(JWT.require(algorithm).build())
        bind(BCryptPasswordEncoder::class.java).toInstance(BCryptPasswordEncoder(12))
        bind(URL::class.java).annotatedWith(Names.named("serverUrl")).toInstance(URL(configuration.serverUrl))
        bind(ComputationService::class.java).asEagerSingleton()
    }

}
