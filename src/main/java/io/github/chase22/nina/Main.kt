package io.github.chase22.nina

import io.github.chase22.nina.database.Warnings
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val LOGGER = LoggerFactory.getLogger(Main::class.java)

        LOGGER.info("Started")

        val databaseUrl: String = System.getenv("NINA_DATABASE_URL") ?: "jdbc:h2:~/ninaTest"
        val databaseDriver: String = System.getenv("NINA_DATABASE_DRIVER") ?: "org.h2.Driver"

        Database.connect(url = databaseUrl, driver = databaseDriver)

        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(Warnings)
        }

        NinaClient.saveWarning()

        LOGGER.info("Finished")
    }
}
