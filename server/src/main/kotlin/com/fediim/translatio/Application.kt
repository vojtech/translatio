package com.fediim.translatio

import com.fediim.translatio.config.ApiConfig
import com.fediim.translatio.db.*
import com.fediim.translatio.di.AppModules
import com.fediim.translatio.feature.auth.apple.appleAuth
import com.fediim.translatio.feature.auth.github.githubAuth
import com.fediim.translatio.feature.auth.google.googleAuth
import com.fediim.translatio.feature.auth.installAuthentication
import com.fediim.translatio.feature.auth.microsoft.microsoftAuth
import com.fediim.translatio.feature.locale.locale
import com.fediim.translatio.feature.locale.translations
import com.fediim.translatio.feature.login.login
import com.fediim.translatio.feature.logout.logout
import com.fediim.translatio.feature.register.register
import com.fediim.translatio.feature.server.status.serverStatus
import com.fediim.translatio.shared.LocaleRequest
import com.fediim.translatio.shared.UserSession
import io.ktor.client.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.receiveMultipart
import io.ktor.server.request.receiveParameters
import io.ktor.server.resources.Resources
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

@OptIn(ExperimentalTime::class)
fun Application.module() {
    configureDatabase()

    install(CallLogging)
    install(AutoHeadResponse)
    install(Compression) { gzip() }
    install(ContentNegotiation) { json() }
    install(Resources)
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.AccessControlAllowOrigin)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
    }
    install(Sessions) {
        cookie<UserSession>("TRANSLATIO_SESSION", storage = SessionStorageMemory()) {
            cookie.path = "/"
            cookie.httpOnly = true
        }
    }
    install(Koin) {
        slf4jLogger()
        modules(AppModules.module)
    }

    val httpClient by inject<HttpClient>()
    val apiConfig by inject<ApiConfig>()

    installAuthentication(httpClient, apiConfig)

    routing {
        serverStatus()
        login()
        register()
        logout()
        githubAuth()
        googleAuth()
        microsoftAuth()
        appleAuth()

        authenticate("auth-session", strategy = AuthenticationStrategy.Required) {
            locale()
            translations()

            route("/import") {
                post {
                    val multipart = call.receiveMultipart()
                    var filename: String? = null
                    var bytes: ByteArray? = null
                    multipart.forEachPart { part ->
                        if (part is PartData.FileItem) {
                            filename = part.originalFileName
                            bytes = part.streamProvider().readBytes()
                        }
                        part.dispose()
                    }
                    val content = bytes ?: return@post call.respond(HttpStatusCode.BadRequest, "No file")
                    val name = (filename ?: "").lowercase()
                    val parsed: List<Pair<String, String>> = when {
                        name.endsWith(".xml") -> Importers.parseAndroidStringsXml(content)
                        name.endsWith(".arb") -> Importers.parseFlutterArb(content)
                        name.endsWith(".strings") -> Importers.parseIosStrings(content)
                        else -> emptyList()
                    }
                    call.respond(HttpStatusCode.Created, parsed)
                }
                post("/confirm") {
                    val params = call.receiveParameters()
                    val keys = params.getAll("k") ?: emptyList()
                    val values = params.getAll("v") ?: emptyList()
                    val lang = params["language"]?.trim().orEmpty()
                    val country = params["country"]?.trim().orEmpty()
                    if (lang.isBlank()) return@post call.respond(HttpStatusCode.BadRequest, "Language required")
                    transaction {
                        val locId = LocaleDb.selectAll().where { (LocaleDb.language eq lang) and (LocaleDb.country eq country) }
                            .singleOrNull()?.get(LocaleDb.id)?.value ?: LocaleDb.insertLocale(LocaleRequest(lang, country), isDefaultFlag = false, isEditableFlag = true)
                        keys.zip(values).forEach { (k, v) ->
                            val sid =
                                StringKeyDb.selectAll().where { StringKeyDb.key eq k }.singleOrNull()?.get(StringKeyDb.id)?.value
                                    ?: StringKeyDb.insertAndGetId {
                                        it[key] = k
                                        it[description] = null
                                        it[createdAt] = Clock.System.now().toEpochMilliseconds()
                                    }.value
                            upsertTranslation(sid, locId, v)
                        }
                        ChangeHistoryDb.insert {
                            it[action] = "IMPORT"
                            it[details] = "Imported ${keys.size} items for ${lang}${if (country.isNotEmpty()) "-" + country else ""}"
                            it[createdAt] = Clock.System.now().toEpochMilliseconds()
                        }
                    }
                    call.respond(HttpStatusCode.OK)
                }
            }

            // Export
            route("/export") {
                post("/run") {
                    val newVersion = transaction {
                        val max = ExportVersionDb.selectAll()
                            .maxOfOrNull { it[ExportVersionDb.version] } ?: 0
                        ExportVersionDb.insertAndGetId {
                            it[version] = max + 1
                            it[createdAt] = Clock.System.now().toEpochMilliseconds()
                        }.value
                    }
                    transaction {
                        ChangeHistoryDb.insert {
                            it[action] = "EXPORT"; it[details] = "version=${'$'}newVersion"; it[createdAt] =
                            Clock.System.now().toEpochMilliseconds()
                        }
                    }
                    call.respond(HttpStatusCode.OK)
                }
                get("/{version}/android") {
                    val ver =
                        call.parameters["version"]?.toIntOrNull() ?: return@get call.respond(HttpStatusCode.BadRequest)
                    val content = Exporters.exportAndroidXml()
                    call.respondText(content, ContentType.Text.Xml)
                }
                get("/{version}/flutter") {
                    val content = Exporters.exportFlutterArb()
                    call.respondText(content, ContentType.Application.Json)
                }
                get("/{version}/ios") {
                    val content = Exporters.exportIosStrings()
                    call.respondText(content, ContentType.parse("text/plain"))
                }
                get("/{version}/web") {
                    val content = Exporters.exportWebJson()
                    call.respondText(content, ContentType.Application.Json)
                }
                get("/locales") {
                    val content = Exporters.exportLocalesJson()
                    call.respondText(content, ContentType.Application.Json)
                }
            }

            get("/history") {
                val hist = transaction {
                    ChangeHistoryDb.selectAll().orderBy(ChangeHistoryDb.createdAt, SortOrder.DESC).limit(100).map { it }
                }
                call.respond(HttpStatusCode.OK, hist)
            }
        }
    }
}