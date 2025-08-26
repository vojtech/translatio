package com.fediim.translatio.feature.server.status

import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

fun Route.serverStatus() {
    get("/health") { call.respond(mapOf("status" to "OK")) }
}