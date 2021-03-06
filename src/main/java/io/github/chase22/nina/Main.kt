package io.github.chase22.nina

import io.github.chase22.nina.Dependencies.meterRegistry
import io.github.chase22.nina.Dependencies.ninaClientFactory
import io.github.chase22.nina.Dependencies.warningMetricsWriter
import io.github.chase22.nina.database.WarningsTable
import io.micrometer.core.instrument.binder.db.DatabaseTableMetrics
import org.h2.jdbcx.JdbcDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.util.concurrent.TimeUnit

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
        val datasource = JdbcDataSource()
        datasource.setUrl("jdbc:h2:~/ninaTest")

        Database.connect(datasource) {
            ThreadLocalTransactionManager(it, Connection.TRANSACTION_READ_COMMITTED, 1)
        }
        DatabaseTableMetrics.monitor(meterRegistry, "WARNINGS", "h2", datasource)

        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(WarningsTable)
        }

        //Setup executor
        val executor = Executor(2);


        urls.stream().forEach {
            logger.info("Adding executor for $it")
            executor.submit(ninaClientFactory.createClient(it), 10, TimeUnit.MINUTES)
        }

        ScrapeMetricsWriter.initialize(urls)
        executor.submit(ScrapeMetricsWriter, 10, TimeUnit.SECONDS)

        executor.submit(warningMetricsWriter, 1, TimeUnit.MINUTES)
        logger.info("Finished")
    }
}
