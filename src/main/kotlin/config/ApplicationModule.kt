package config

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.inject.AbstractModule
import com.google.inject.name.Names
import model.enums.Environment
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.net.URL

class ApplicationModule(private val configuration: SpaceConfiguration) : AbstractModule() {

    override fun configure() {
        val mapper: ObjectMapper = jacksonObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(JavaTimeModule())
        bind(ObjectMapper::class.java).toInstance(mapper)
        bind(Environment::class.java).toInstance(configuration.environment)
        val algorithm = Algorithm.HMAC256(configuration.jwtSecret)
        bind(Algorithm::class.java).toInstance(algorithm)
        bind(JWTVerifier::class.java).toInstance(JWT.require(algorithm).build())
        bind(BCryptPasswordEncoder::class.java).toInstance(BCryptPasswordEncoder(12))
        bind(URL::class.java).annotatedWith(Names.named("serverUrl")).toInstance(URL(configuration.serverUrl))
    }

}