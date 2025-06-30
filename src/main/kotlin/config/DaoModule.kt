package config

import com.google.inject.AbstractModule
import dao.*
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.core.kotlin.KotlinPlugin
import org.jdbi.v3.postgres.UUIDArgumentFactory
import org.jdbi.v3.sqlobject.kotlin.KotlinSqlObjectPlugin

class DaoModule(private val database: DatabaseConfig) : AbstractModule() {

    override fun configure() {
        val jdbi = Jdbi.create(database.url, database.user, database.password)
        jdbi.installPlugin(KotlinPlugin())
        jdbi.installPlugin(KotlinSqlObjectPlugin())
        jdbi.registerArgument(UUIDArgumentFactory())
        bindDao(jdbi, ComputationDao::class.java)
        bindDao(jdbi, ConclusionDao::class.java)
        bindDao(jdbi, ConditionDao::class.java)
        bindDao(jdbi, DifferentialEquationDao::class.java)
        bindDao(jdbi, DifferentialEquationLinkDao::class.java)
        bindDao(jdbi, DifferentialEquationPropertyDao::class.java)
        bindDao(jdbi, InviteDao::class.java)
        bindDao(jdbi, LinkDao::class.java)
        bindDao(jdbi, PropertyDao::class.java)
        bindDao(jdbi, ReferenceDao::class.java)
        bindDao(jdbi, SpaceDao::class.java)
        bindDao(jdbi, TheoremDao::class.java)
        bindDao(jdbi, UserDao::class.java)
    }

    private fun <T> bindDao(jdbi: Jdbi, clazz: Class<T>) {
        bind(clazz).toInstance(jdbi.onDemand(clazz))
    }

}
