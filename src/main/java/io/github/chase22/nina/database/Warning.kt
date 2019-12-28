package io.github.chase22.nina.database

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import java.util.*

class Warning(id: EntityID<UUID>): UUIDEntity(id) {
    companion object:UUIDEntityClass<Warning>(WarningsTable)

    var warningId by WarningsTable.id
    var identifier by WarningsTable.identifier
    var json by WarningsTable.json
    var version by WarningsTable.version
}