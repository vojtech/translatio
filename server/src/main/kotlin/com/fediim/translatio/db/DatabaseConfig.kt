package com.fediim.translatio.db

import io.ktor.server.application.Application

fun Application.configureDatabase() {
    val dbUrl = System.getenv("DATABASE_URL") ?: "jdbc:postgresql://localhost:5432/translatio"
    val dbUser = System.getenv("POSTGRES_USER") ?: "postgres"
    val dbPass = System.getenv("POSTGRES_PASSWORD") ?: "postgres"
    DatabaseFactory.init(dbUrl, dbUser, dbPass)
}