package com.fediim.translatio

import com.fediim.translatio.shared.*
import com.fediim.translatio.util.Logger
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import io.ktor.client.plugins.logging.Logger as KtorLogger

expect fun platformBaseUrl(): String
expect fun platformHttpClient(): HttpClient

class Api(val appLogger: Logger) {
    private val base = platformBaseUrl()
    private val client: HttpClient = platformHttpClient().config {
        install(ContentNegotiation) { json() }
        install(HttpCookies) {
            storage = AcceptAllCookiesStorage()
        }
        install(Logging) {
            level = LogLevel.ALL
            logger = object : KtorLogger {
                override fun log(message: String) {
                    appLogger.log("Api Client") { message }
                }
            }
        }
        defaultRequest {
            url {
                protocol = URLProtocol.HTTP
                host = "localhost"
                port = 8080
            }
        }
    }

    suspend fun health(): String = client.get("$base/health").body<Map<String, String>>()["status"] ?: ""

    suspend fun login(loginRequest: LoginRequest): LoginResponse =
        client.post("$base/login") {
            contentType(ContentType.Application.Json)
            setBody(loginRequest)
        }.body()

    suspend fun register(registerRequest: RegisterRequest): RegisterResponse =
        client.post("$base/register") {
            contentType(ContentType.Application.Json)
            setBody(registerRequest)
        }.body()

    suspend fun logout() {
        client.post("$base/logout")
    }

    suspend fun locales(): List<InternalLocale> = client.get("$base/locales").body()

    suspend fun addLocale(localeRequest: LocaleRequest): InternalLocale =
        client.post("$base/locales") {
            contentType(ContentType.Application.Json)
            setBody(localeRequest)
        }.body()
    suspend fun createStringKey(key: String, description: String?): Int? {
        val resp = client.post("$base/strings/new") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(Parameters.build {
                append("key", key)
                description?.let { append("description", it) }
            }.formUrlEncode())
        }
        if (!resp.status.isSuccess()) return null
        // fetch id by key from /strings which returns array of [id, key, count]
        return findStringIdByKey(key)
    }

    private suspend fun findStringIdByKey(key: String): Int? {
        // We expect json array of arrays
        val arr: JsonArray = client.get("$base/strings").body()
        for (row in arr) {
            val list = row as? JsonArray ?: continue
            if (list.size >= 2) {
                val idEl: JsonElement = list[0]
                val keyEl: JsonElement = list[1]
                val keyStr = (keyEl as? JsonPrimitive)?.content
                val idVal = (idEl as? JsonPrimitive)?.content?.toIntOrNull()
                if (keyStr == key && idVal != null) return idVal
            }
        }
        return null
    }

    suspend fun submitTranslation(stringId: Int, localeId: Int, value: String) {
        client.post("$base/strings/$stringId/translate") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(Parameters.build {
                append("localeId", localeId.toString())
                append("value", value)
            }.formUrlEncode())
        }
    }
}
