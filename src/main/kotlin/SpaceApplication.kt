import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.google.inject.Guice
import config.ApplicationModule
import config.DaoModule
import config.SpaceConfiguration
import filter.HttpsRedirectFilter
import filter.NotFoundFilter
import filter.SecurityHeadersFilter
import filter.request.CsrfRequestFilter
import filter.request.JwtAuthRequestFilter
import filter.request.OriginRequestFilter
import filter.response.CsrfResponseFilter
import io.dropwizard.Application
import io.dropwizard.assets.AssetsBundle
import io.dropwizard.auth.AuthDynamicFeature
import io.dropwizard.auth.AuthValueFactoryProvider
import io.dropwizard.configuration.EnvironmentVariableSubstitutor
import io.dropwizard.configuration.SubstitutingSourceProvider
import io.dropwizard.forms.MultiPartBundle
import io.dropwizard.jdbi3.JdbiFactory
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import model.User
import org.flywaydb.core.Flyway
import org.glassfish.jersey.logging.LoggingFeature
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature
import org.jdbi.v3.postgres.PeriodArgumentFactory
import org.jdbi.v3.postgres.UUIDArgumentFactory
import resource.*
import service.DevStartupService
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import javax.servlet.DispatcherType


class SpaceApplication : Application<SpaceConfiguration>() {

    override fun initialize(bootstrap: Bootstrap<SpaceConfiguration>) {
        bootstrap.addBundle(AssetsBundle("/assets/", "/", "index.html"))
        bootstrap.addBundle(MultiPartBundle())
        bootstrap.configurationSourceProvider = SubstitutingSourceProvider(
            bootstrap.configurationSourceProvider,
            EnvironmentVariableSubstitutor()
        )
    }

    override fun run(configuration: SpaceConfiguration, environment: Environment) {
        val env = configuration.environment!!
        if (env == model.enums.Environment.DEVELOPMENT) {
            environment.jersey().register(
                LoggingFeature(
                    Logger.getLogger("inbound"),
                    Level.INFO, LoggingFeature.Verbosity.PAYLOAD_ANY, 8192
                )
            )
        }

        val flyway = Flyway.configure()
            .dataSource(
                configuration.database.url, configuration.database.user,
                configuration.database.password
            )
            .load()
        flyway.migrate()

        environment.objectMapper.registerModule(KotlinModule())

        val jdbi = JdbiFactory().build(environment, configuration.database, "postgresql")
        jdbi.installPlugins()
        jdbi.registerArgument(UUIDArgumentFactory())
        jdbi.registerArgument(PeriodArgumentFactory())

        val injector = Guice.createInjector(DaoModule(jdbi), ApplicationModule(configuration))

        val jersey = environment.jersey()
        jersey.register(injector.getInstance(AdminResource::class.java))
        jersey.register(injector.getInstance(SpaceResource::class.java))
        jersey.register(injector.getInstance(PropertyResource::class.java))
        jersey.register(injector.getInstance(TheoremResource::class.java))
        jersey.register(injector.getInstance(UserResource::class.java))

        val originRequestFilter = injector.getInstance(OriginRequestFilter::class.java)
        environment.jersey().register(originRequestFilter)

        val csrfRequestFilter = injector.getInstance(CsrfRequestFilter::class.java)
        environment.jersey().register(csrfRequestFilter)

        val jwtAuthFilter = injector.getInstance(JwtAuthRequestFilter::class.java)
        environment.jersey().register(AuthDynamicFeature(jwtAuthFilter))
        environment.jersey().register(RolesAllowedDynamicFeature::class.java)
        environment.jersey().register(AuthValueFactoryProvider.Binder(User::class.java))

        val csrfResponseFilter = injector.getInstance(CsrfResponseFilter::class.java)
        environment.jersey().register(csrfResponseFilter)

        val securityHeadersFilter = injector.getInstance(SecurityHeadersFilter::class.java)
        environment.servlets().addFilter("SecurityHeadersFilter", securityHeadersFilter)
            .addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*")

        if (configuration.httpsRedirect) {
            val httpsRedirectFilter = injector.getInstance(HttpsRedirectFilter::class.java)
            environment.servlets().addFilter("HttpsRedirectFilter", httpsRedirectFilter)
                .addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*")
        }

        val notFoundFilter = injector.getInstance(NotFoundFilter::class.java)
        environment.servlets().addFilter("NotFoundFilter", notFoundFilter)
            .addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*")

        if (env == model.enums.Environment.DEVELOPMENT) {
            injector.getInstance(DevStartupService::class.java).startup()
        }

    }
}

fun main(args: Array<String>) {
    SpaceApplication().run(*args)
}