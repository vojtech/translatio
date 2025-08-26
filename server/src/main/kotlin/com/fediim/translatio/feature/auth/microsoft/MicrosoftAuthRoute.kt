package com.fediim.translatio.feature.auth.microsoft

import com.fediim.translatio.application.AuthService
import com.fediim.translatio.config.ApiConfig
import com.fediim.translatio.shared.Role
import com.fediim.translatio.shared.User
import com.fediim.translatio.shared.UserSession
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headers
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set
import kotlinx.serialization.json.jsonObject
import org.koin.ktor.ext.inject
import kotlin.getValue

fun Route.microsoftAuth() {
    val apiConfig by inject<ApiConfig>()
    val httpClient by inject<HttpClient>()
    val authService by inject<AuthService>()

    if (apiConfig.microsoftEnabled) authenticate("microsoft") {
        get("/oauth/microsoft/start") { }
        get("/oauth/microsoft/callback") {
            val principal = call.principal<io.ktor.server.auth.OAuthAccessTokenResponse.OAuth2>()
            if (principal == null) return@get call.respond(HttpStatusCode.Unauthorized)
            val token = principal.accessToken
            val json = kotlinx.serialization.json.Json.parseToJsonElement(
                httpClient.get("https://graph.microsoft.com/oidc/userinfo") {
                    headers { append(HttpHeaders.Authorization, "Bearer $token") }
                }.bodyAsText()
            ).jsonObject
            val oid = json["sub"]?.toString()?.trim('"') ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val email = json["email"]?.toString()?.trim('"') ?: json["preferred_username"]?.toString()?.trim('"')
            val given = json["given_name"]?.toString()?.trim('"')
            val family = json["family_name"]?.toString()?.trim('"')
            val username = json["name"]?.toString()?.trim('"') ?: (email ?: ("microsoft_" + oid))
            val res = authService.loginOrRegisterOAuth(
                "microsoft",
                oid,
                User(username = username, email = email, firstName = given, lastName = family, role = Role.viewer.name)
            )
            call.sessions.set(UserSession(res.userId, res.username))
            call.respondRedirect("/dashboard")
        }
    }

    if (!apiConfig.microsoftEnabled) get("/oauth/microsoft/start") {
        call.respond(
            HttpStatusCode.NotImplemented,
            "Microsoft OAuth is not configured"
        )
    }
}