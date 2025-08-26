package com.fediim.translatio.feature.auth.apple

import com.fediim.translatio.application.AuthService
import com.fediim.translatio.config.ApiConfig
import com.fediim.translatio.shared.Role
import com.fediim.translatio.shared.User
import com.fediim.translatio.shared.UserSession
import io.ktor.http.HttpStatusCode
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

fun Route.appleAuth() {
    val apiConfig by inject<ApiConfig>()
    val authService by inject<AuthService>()

    if (apiConfig.appleEnabled) authenticate("apple") {
        get("/oauth/apple/start") { }
        get("/oauth/apple/callback") {
            val principal = call.principal<io.ktor.server.auth.OAuthAccessTokenResponse.OAuth2>()
            if (principal == null) return@get call.respond(HttpStatusCode.Unauthorized)
            val idToken = principal.extraParameters["id_token"]
            val payload = idToken?.split(".")?.getOrNull(1)?.let { java.util.Base64.getUrlDecoder().decode(it) }
                ?.toString(Charsets.UTF_8)
            val json = payload?.let { kotlinx.serialization.json.Json.parseToJsonElement(it).jsonObject }
            val oid =
                json?.get("sub")?.toString()?.trim('"') ?: return@get call.respond(HttpStatusCode.Unauthorized)
            val email = json["email"]?.toString()?.trim('"')
            val username = email ?: ("apple_" + oid)
            val res = authService.loginOrRegisterOAuth(
                "apple",
                oid,
                User(username = username, email = email, firstName = null, lastName = null, role = Role.viewer.name)
            )
            call.sessions.set(UserSession(res.userId, res.username))
            call.respondRedirect("/dashboard")
        }
    }

    if (!apiConfig.appleEnabled) get("/oauth/apple/start") {
        call.respond(
            HttpStatusCode.NotImplemented,
            "Apple OAuth is not configured"
        )
    }
}