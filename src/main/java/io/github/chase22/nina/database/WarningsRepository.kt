package io.github.chase22.nina.database

import io.github.chase22.nina.JsonHashFactory
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

class WarningsRepository(private val hashFactory: JsonHashFactory) {
    private val logger = LoggerFactory.getLogger(WarningsRepository::class.java)

    fun getCurrentVersion(identifier: String): Int? {
        return transaction {
            return@transaction Warning.find { WarningsTable.identifier eq identifier }
                    .maxBy { it.version }?.version
        }
    }

    fun get(identifier: String, version: Int): Warning {
        return transaction {
            return@transaction Warning.find {
                WarningsTable.identifier eq identifier and (WarningsTable.version eq version)
            }.first()
        }
    }

    fun create(identifier: String, json: String, version: Int) {
        transaction {
            Warning.new {
                this.identifier = identifier
                this.json = json
                this.version = version
            }
        }
    }

    fun addJson(identifier: String, json: String) {
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