package io.github.chase22.nina.database

import org.jetbrains.exposed.dao.UUIDTable
import java.time.ZoneId
import java.time.ZonedDateTime

object WarningsTable : UUIDTable() {
    val identifier = varchar("identifier", 100)
    val version = integer("version")
    val json = text("json")
    val createdDate = varchar("createdDate", 28).clientDefault {
        ZonedDateTime.now(ZoneId.of("UTC")).toOffsetDateTime().toString()
    }
}