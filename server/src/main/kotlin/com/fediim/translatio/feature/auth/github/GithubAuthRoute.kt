package com.fediim.translatio.feature.auth.github

import com.fediim.translatio.application.AuthService
import com.fediim.translatio.config.ApiConfig
import com.fediim.translatio.shared.Role
import com.fediim.translatio.shared.User
import com.fediim.translatio.shared.UserSession
import io.ktor.client.*
import io.ktor.client.request.get
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.headers
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.serialization.json.jsonObject
import org.koin.ktor.ext.inject

fun Route.githubAuth() {
    val apiConfig by inject<ApiConfig>()
    val httpClient by inject<HttpClient>()
    val authService by inject<AuthService>()

    if (apiConfig.githubEnabled) authenticate("github") {
        get("/oauth/github/start") { /* triggers redirect */ }
        get("/oauth/github/callback") {
            val principal = call.principal<io.ktor.server.auth.OAuthAccessTokenResponse.OAuth2>()
            if (principal == null) return@get call.respond(HttpStatusCode.Unauthorized)
            val token = principal.accessToken
            val body = httpClient.get("https://api.github.com/user") {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token"); append(
                    "Accept",
                    "application/vnd.github+json"
                )
                }
            }.bodyAsText()
            val json = kotlinx.serialization.json.Json.parseToJsonElement(body).jsonObject
            val oid = json["id"]?.toString()?.trim('"') ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val login = json["login"]?.toString()?.trim('"') ?: ("github_" + oid)
            val name = json["name"]?.toString()?.trim('"')
            val (first, last) = name?.let { it.split(" ", limit = 2).let { p -> p.getOrNull(0) to p.getOrNull(1) } }
                ?: (null to null)
            val email = json["email"]?.toString()?.trim('"')
            val res = authService.loginOrRegisterOAuth(
                "github",
                oid,
                User(username = login, email = email, firstName = first, lastName = last, role = Role.viewer.name)
            )
            call.sessions.set(UserSession(res.userId, res.username))
            call.respondRedirect("/dashboard")
        }
    }

    if (!apiConfig.githubEnabled) get("/oauth/github/start") {
        call.respond(
            HttpStatusCode.NotImplemented,
            "GitHub OAuth is not configured"
        )
    }
}