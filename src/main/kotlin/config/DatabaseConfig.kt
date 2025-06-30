package config

data class DatabaseConfig(
    var user: String? = null,
    var password: String? = null,
    var url: String? = null
)
