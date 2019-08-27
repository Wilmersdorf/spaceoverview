package config

import com.google.inject.AbstractModule
import dao.*
import org.jdbi.v3.core.Jdbi

class DaoModule(private val jdbi: Jdbi) : AbstractModule() {

    override fun configure() {
        bindDao(SpaceDao::class.java)
        bindDao(PropertyDao::class.java)
        bindDao(LinkDao::class.java)
        bindDao(ReferenceDao::class.java)
        bindDao(UserDao::class.java)
        bindDao(InviteDao::class.java)
    }

    private fun <T> bindDao(clazz: Class<T>) {
        bind(clazz).toInstance(jdbi.onDemand(clazz))
    }
}