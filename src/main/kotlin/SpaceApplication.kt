import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.google.inject.Guice
import config.ApplicationModule
import config.DaoModule
import config.SpaceConfiguration
import filter.request.CsrfRequestFilter
import filter.request.JwtAuthRequestFilter
import filter.request.OriginRequestFilter
import filter.response.CsrfResponseFilter
import io.dropwizard.auth.AuthDynamicFeature
import io.dropwizard.auth.AuthValueFactoryProvider
import io.dropwizard.core.Application
import io.dropwizard.core.setup.Environment
import io.dropwizard.jersey.jackson.JsonProcessingExceptionMapper
import model.User
import org.flywaydb.core.Flyway
import org.glassfish.jersey.logging.LoggingFeature
import resource.*
import service.DevStartupService
import java.util.logging.Level
import java.util.logging.Logger

class SpaceApplication : Application<SpaceConfiguration>() {

    override fun run(configuration: SpaceConfiguration, environment: Environment) {
        val env = configuration.environment!!
        val jersey = environment.jersey()

        val database = configuration.database!!
        val flyway = Flyway.configure().dataSource(database.url, database.user, database.password).load()
        flyway.migrate()

        environment.objectMapper.registerModule(KotlinModule.Builder().build())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(JavaTimeModule())

        val injector = Guice.createInjector(DaoModule(database), ApplicationModule(configuration))

        jersey.register(injector.getInstance(AdminResource::class.java))
        jersey.register(injector.getInstance(DifferentialEquationPropertyResource::class.java))
        jersey.register(injector.getInstance(DifferentialEquationResource::class.java))
        jersey.register(injector.getInstance(PropertyResource::class.java))
        jersey.register(injector.getInstance(SpaceResource::class.java))
        jersey.register(injector.getInstance(TheoremResource::class.java))
        jersey.register(injector.getInstance(UserResource::class.java))

        val originRequestFilter = injector.getInstance(OriginRequestFilter::class.java)
        environment.jersey().register(originRequestFilter)

        val csrfRequestFilter = injector.getInstance(CsrfRequestFilter::class.java)
        environment.jersey().register(csrfRequestFilter)

        val jwtAuthFilter = injector.getInstance(JwtAuthRequestFilter::class.java)
        environment.jersey().register(AuthDynamicFeature(jwtAuthFilter))
        environment.jersey().register(AuthValueFactoryProvider.Binder(User::class.java))

        val csrfResponseFilter = injector.getInstance(CsrfResponseFilter::class.java)
        environment.jersey().register(csrfResponseFilter)

        if (env == model.enums.Environment.DEVELOPMENT) {
            jersey.register(JsonProcessingExceptionMapper(true))
            jersey.register(
                LoggingFeature(
                    Logger.getLogger("inbound"),
                    Level.INFO, LoggingFeature.Verbosity.PAYLOAD_ANY, 8192
                )
            )
            injector.getInstance(DevStartupService::class.java).startup()
        }
    }

}

fun main(args: Array<String>) {
    SpaceApplication().run(*args)
}
