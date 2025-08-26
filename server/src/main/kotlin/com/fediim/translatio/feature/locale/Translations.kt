package com.fediim.translatio.feature.locale

import com.fediim.translatio.db.*
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Resource("translations")
class Translations

@Resource("translations/{id}")
class TranslationById(val id: Int)


@Resource("translations/new")
class CreateTranslationKey(val id: Int)

@Resource("translations/new")
class AddTranslations(val id: Int)

@OptIn(ExperimentalTime::class)
fun Routing.translations() {

    route("/strings") {
        get {
            val rows = transaction {
                StringKeyDb.selectAll().orderBy(StringKeyDb.key).map { row ->
                    val trCount =
                        TranslationDb.selectAll().where { TranslationDb.stringId eq row[StringKeyDb.id].value }.count()
                    Triple(row[StringKeyDb.id].value, row[StringKeyDb.key], trCount)
                }
            }
            call.respond(rows)
        }
        post("/new") {
            val params = call.receiveParameters()
            val key = params["key"]?.trim().orEmpty()
            val desc = params["description"]?.trim()
            if (key.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Key required"); return@post
            }
            val id = transaction {
                StringKeyDb.insertAndGetId {
                    it[this.key] = key
                    it[this.description] = desc
                    it[this.createdAt] = Clock.System.now().toEpochMilliseconds()
                }.value
            }
            // Create empty translations for all existing locales so the new string is reflected everywhere
            transaction {
                val locales = LocaleDb.selectAll().map { it[LocaleDb.id].value }
                locales.forEach { locId ->
                    upsertTranslation(id, locId, "")
                }
            }
            transaction {
                ChangeHistoryDb.insert {
                    it[action] = "CREATE_KEY"; it[details] = key; it[createdAt] =
                    Clock.System.now().toEpochMilliseconds()
                }
            }
            call.respond(HttpStatusCode.OK)
        }
        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
            val data = transaction {
                val keyRow = StringKeyDb.selectAll().where { StringKeyDb.id eq id }.singleOrNull() ?: return@transaction null
                val locales = LocaleDb.selectAll().map { it }
                val translations = TranslationDb.selectAll().where { TranslationDb.stringId eq id }
                    .associateBy { it[TranslationDb.localeId].value }
                Triple(keyRow, locales, translations)
            } ?: return@get call.respond(HttpStatusCode.NotFound)
            call.respond(HttpStatusCode.OK, data)
        }
        post("/{id}/translate") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@post call.respond(HttpStatusCode.BadRequest)
            val params = call.receiveParameters()
            val localeId =
                params["localeId"]?.toIntOrNull() ?: return@post call.respond(HttpStatusCode.BadRequest)
            val value = params["value"]?.trim().orEmpty()
            transaction { upsertTranslation(id, localeId, value) }
            transaction {
                ChangeHistoryDb.insert {
                    it[action] = "UPDATE_TRANSLATION"; it[details] =
                    "string=${'$'}id;locale=${'$'}localeId"; it[createdAt] =
                    Clock.System.now().toEpochMilliseconds()
                }
            }
            call.respond(HttpStatusCode.OK)
        }
    }
}
