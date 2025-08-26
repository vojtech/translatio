package com.fediim.translatio.feature.register

import com.fediim.translatio.application.AuthService
import com.fediim.translatio.shared.RegisterRequest
import com.fediim.translatio.shared.RegisterResponse
import com.fediim.translatio.shared.Role
import com.fediim.translatio.shared.UserSession
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.request.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.Routing
import io.ktor.server.sessions.*
import org.koin.ktor.ext.inject

@Resource("register")
class Register()

fun Routing.register() {
    val authService by inject<AuthService>()
    post<Register> {
        val user = call.receive<RegisterRequest>()
        try {
            val id = authService.register(user = user, role = Role.viewer)
            call.sessions.set(UserSession(id, user.email))
            call.respond(RegisterResponse(userId = id, username = user.email))
        } catch (e: IllegalArgumentException) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
        }
    }
}