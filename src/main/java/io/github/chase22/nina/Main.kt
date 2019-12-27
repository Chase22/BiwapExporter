package io.github.chase22.nina

import io.github.chase22.nina.database.Warnings
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val logger = LoggerFactory.getLogger(Main::class.java)

        val baseUrl = "https://warnung.bund.de"

        val urls: List<String> = listOf(
                "$baseUrl/bbk.biwapp/warnmeldungen.json",
                "$baseUrl/bbk.katwarn/warnmeldungen.json",
                "$baseUrl/bbk.dwd/unwetter.json",
                "$baseUrl/bbk.lhp/hochwassermeldungen.json",
                "$baseUrl/bbk.mowas/gefahrendurchsagen.json"
        )

        logger.info("Started")

        // Setup Database
        val databaseUrl: String = System.getenv("NINA_DATABASE_URL") ?: "jdbc:h2:~/ninaTest"
        val databaseDriver: String = System.getenv("NINA_DATABASE_DRIVER") ?: "org.h2.Driver"

        Database.connect(url = databaseUrl, driver = databaseDriver)

        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(Warnings)
        }

        //Setup executor
        val executor = Executor(urls.size);

        urls.stream().forEach {
            logger.info("Adding executor for $it")
            executor.submit(NinaClient(it), 10)
        }

        logger.info("Finished")
    }
}
