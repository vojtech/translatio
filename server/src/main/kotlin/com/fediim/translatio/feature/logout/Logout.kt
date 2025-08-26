package com.fediim.translatio.feature.logout

import com.fediim.translatio.shared.UserSession
import io.ktor.http.*
import io.ktor.resources.*
import io.ktor.server.resources.post
import io.ktor.server.response.*
import io.ktor.server.routing.Routing
import io.ktor.server.sessions.*

@Resource("logout")
class Logout()

fun Routing.logout() {
    post<Logout> {
        call.sessions.clear<UserSession>()
        call.respond(HttpStatusCode.OK)
    }
}