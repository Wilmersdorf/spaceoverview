package config

import io.dropwizard.Configuration
import io.dropwizard.db.DataSourceFactory
import model.enums.Environment

class SpaceConfiguration(
    val database: DataSourceFactory = DataSourceFactory(),
    val environment: Environment? = null,
    val jwtSecret: String? = null,
    val serverUrl: String? = null,
    val httpsRedirect: Boolean = true
) : Configuration()