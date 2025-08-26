package com.fediim.translatio.feature.login

import com.fediim.translatio.application.AuthService
import com.fediim.translatio.shared.LoginRequest
import com.fediim.translatio.shared.UserSession
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.request.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.Routing
import io.ktor.server.sessions.*
import org.koin.ktor.ext.inject

@Resource("login")
class Login()

fun Routing.login() {
    val authService by inject<AuthService>()
    
    post<Login> {
        val request = call.receive<LoginRequest>()
        val res = authService.login(request.username, request.password)
        if (res == null) {
            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid credentials"))
        } else {
            call.sessions.set(UserSession(res.userId, res.username))
            call.respond(HttpStatusCode.OK, UserSession(res.userId, res.username))
        }
    }
}