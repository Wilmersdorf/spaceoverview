package config

import io.dropwizard.core.Configuration
import model.enums.Environment

class SpaceConfiguration(
    val database: DatabaseConfig? = null,
    val environment: Environment? = null,
    val jwtSecret: String? = null,
    val serverUrl: String? = null,
) : Configuration()
