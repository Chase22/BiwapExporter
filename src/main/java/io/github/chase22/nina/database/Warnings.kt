package io.github.chase22.nina.database

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.ZoneId
import java.time.ZonedDateTime

object Warnings : Table() {
    val id = integer("id").autoIncrement().primaryKey()
    val identifier = varchar("identifier", 100)
    val json = text("json")
    val createdDate = varchar("createdDate", 28)

    fun addJson(identifier: String, json: String) {

        val createdDate = ZonedDateTime.now(ZoneId.of("UTC")).toOffsetDateTime().toString()

        transaction {
            insert {
                it[Warnings.identifier] = identifier
                it[Warnings.json] = json
                it[Warnings.createdDate] = createdDate
            }
        }
    }
}