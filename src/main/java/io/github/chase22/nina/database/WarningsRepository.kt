package io.github.chase22.nina.database

import io.github.chase22.nina.JsonHashFactory
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

class WarningsRepository(private val hashFactory: JsonHashFactory) {
    private val logger = LoggerFactory.getLogger(WarningsRepository::class.java)

    fun getCurrentVersion(identifier: String): Int {
        return Warning.find { WarningsTable.identifier eq identifier }
                .maxBy { it.version }?.version ?: 0
    }

    fun getIdentifiers(): List<String> {
        return WarningsTable
                .slice(WarningsTable.identifier)
                .selectAll().withDistinct()
                .map { it[WarningsTable.identifier] }
    }

    fun get(identifier: String, version: Int): Warning {
        return Warning.find {
            WarningsTable.identifier eq identifier and (WarningsTable.version eq version)
        }.toList().single()
    }

    private fun create(identifier: String, json: String, version: Int) {
        Warning.new {
            this.identifier = identifier
            this.json = json
            this.version = version
        }
    }

    fun addJson(identifier: String, json: String) {
        transaction {
            val currentVersion = getCurrentVersion(identifier)
            if (currentVersion != null) {
                val currentJson = get(identifier, currentVersion).json
                if (!hashFactory.getHash(currentJson).contentEquals(hashFactory.getHash(json))) {
                    logger.info("Writing new version ${currentVersion + 1} for ${identifier}")
                    create(identifier, json, currentVersion + 1)
                }
            } else {
                create(identifier, json, 0)
            }
        }
    }
}