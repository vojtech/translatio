package com.fediim.translatio.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object DatabaseFactory {
    fun init(jdbcUrl: String, dbUser: String?, dbPassword: String?) {
        val config = HikariConfig().apply {
            jdbcUrl.let { this.jdbcUrl = it }
            username = dbUser
            password = dbPassword
            maximumPoolSize = 5
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        var lastError: Exception? = null
        // Retry up to ~30 seconds waiting for Postgres to accept connections
        for (attempt in 1..30) {
            try {
                val dataSource = HikariDataSource(config)
                Database.connect(dataSource)
                transaction {
                    SchemaUtils.createMissingTablesAndColumns(
                        UserDb,
                        LocaleDb,
                        StringKeyDb,
                        TranslationDb,
                        ChangeHistoryDb,
                        ExportVersionDb
                    )
                }
                // Seed default admin (env overrides). If not provided, use admin/password
                val adminUser = (System.getenv("ADMIN_USER") ?: "admin").trim()
                val adminPass = (System.getenv("ADMIN_PASS") ?: "password")
                transaction {
                    val exists = UserDb.select(UserDb.username eq adminUser).empty().not()
                    if (!exists) {
                        UserDb.insertUser(adminUser, adminPass, isAdmin = true)
                    }
                }
                // Seed system locales from resources/locales.json
                transaction {
                    if (LocaleDb.selectAll().empty()) {
                        val file = java.io.File("./../resources/locales.json")
                        if (file.exists()) {
                            val content = file.readText()
                            val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                            val items = json.decodeFromString(kotlinx.serialization.builtins.ListSerializer(com.fediim.translatio.model.LocaleExport.serializer()), content)
                            val seen = mutableSetOf<Pair<String, String>>()
                            items.forEach { loc ->
                                val lang = loc.language?.lowercase()?.trim().orEmpty()
                                if (lang.isNotBlank()) {
                                    val country = (loc.country ?: "").lowercase().trim()
                                    val key = lang to country
                                    if (seen.add(key)) {
                                        val isDefault = (lang == "en" && country.isEmpty())
                                        val id = LocaleDb.insertAndGetId {
                                            it[LocaleDb.language] = lang
                                            it[LocaleDb.country] = if (country.isEmpty()) "" else country.uppercase()
                                            it[LocaleDb.isDefault] = isDefault
                                            it[LocaleDb.isEditable] = false
                                        }.value
                                    }
                                }
                            }
                            // Ensure default en with empty country exists
                            val hasDefault = LocaleDb.selectAll().where { (LocaleDb.language eq "en") and (LocaleDb.country eq "") }.empty().not()
                            if (!hasDefault) {
                                LocaleDb.insertSystemLocale("en", "")
                            }
                        } else {
                            // Fallback: ensure default en (no country)
                            val hasDefault = LocaleDb.selectAll().where { (LocaleDb.language eq "en") and (LocaleDb.country eq "") }.empty().not()
                            if (!hasDefault) {
                                LocaleDb.insertSystemLocale("en", "")
                            }
                        }
                    }
                }
                if (attempt > 1) {
                    println("[DatabaseFactory] Connected to DB after $attempt attempts")
                }
                return
            } catch (e: Exception) {
                lastError = e
                println("[DatabaseFactory] DB not ready yet (attempt $attempt): ${e.message}")
                try {
                    Thread.sleep(1000)
                } catch (_: InterruptedException) {
                    break
                }
            }
        }
        println("[DatabaseFactory] Skipping DB init due to error: ${lastError?.message}")
    }
}
