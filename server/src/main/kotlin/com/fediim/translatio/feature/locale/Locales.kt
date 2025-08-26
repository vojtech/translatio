package com.fediim.translatio.feature.locale

import com.fediim.translatio.application.LocaleService
import com.fediim.translatio.db.LocaleDb
import com.fediim.translatio.db.TranslationDb
import com.fediim.translatio.shared.InternalLocale
import com.fediim.translatio.shared.LocaleRequest
import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Routing
import io.ktor.server.resources.post
import io.ktor.server.resources.get
import io.ktor.server.resources.delete
import io.ktor.server.resources.put
import org.jetbrains.exposed.v1.core.eq
import org.koin.ktor.ext.inject
import kotlin.getValue
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction


@Resource("locales")
class Locales

@Resource("locales/{id}")
class LocaleById(val id: Int)

fun Routing.locale() {
    val localeService by inject<LocaleService>()

    get<Locales> {
        call.respond(localeService.list())
    }
    post<Locales> {
        try {
            val locale = call.receive<LocaleRequest>()
            val id = localeService.add(locale)
            call.respond(InternalLocale(id, locale.language, locale.country, isDefault = false, editable = true))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Language required"))
        }
    }

    delete<LocaleById> {
        val id = it.id
        val deleted = transaction {
            val row = LocaleDb.selectAll().where { LocaleDb.id eq id }.singleOrNull()
                ?: return@transaction -1
            val editable = row[LocaleDb.isEditable]
            if (!editable) return@transaction -2
            // delete translations first
            TranslationDb.deleteWhere { TranslationDb.localeId eq id }
            LocaleDb.deleteWhere { LocaleDb.id eq id }
        }
        when (deleted) {
            -1 -> call.respond(HttpStatusCode.NotFound)
            -2 -> call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Locale is not editable"))
            else -> call.respond(HttpStatusCode.OK)
        }
    }

    put<LocaleById> {
        val id = it.id
        try {
            val req = call.receive<LocaleRequest>()
            val result = transaction {
                val row = LocaleDb.selectAll().where { LocaleDb.id eq id }.singleOrNull()
                    ?: return@transaction null
                if (!row[LocaleDb.isEditable]) return@transaction "NOT_EDITABLE"
                // attempt update
                val count = LocaleDb.update({ LocaleDb.id eq id }) {
                    it[LocaleDb.language] = req.language
                    it[LocaleDb.country] = req.country
                }
                if (count > 0) "OK" else "NO_CHANGE"
            }
            when (result) {
                null -> call.respond(HttpStatusCode.NotFound)
                "NOT_EDITABLE" -> call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Locale is not editable"))
                else -> call.respond(HttpStatusCode.OK)
            }
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Invalid request")))
        }
    }
}