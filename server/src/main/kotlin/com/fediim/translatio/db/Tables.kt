package com.fediim.translatio.db

import at.favre.lib.crypto.bcrypt.BCrypt
import com.fediim.translatio.shared.LocaleRequest
import com.fediim.translatio.shared.User
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

object UserDb : IntIdTable("users") {
    val username = varchar("username", 128).uniqueIndex()
    val email = varchar("email", 256).nullable().uniqueIndex()
    val firstName = varchar("first_name", 128).nullable()
    val lastName = varchar("last_name", 128).nullable()
    val role = varchar("role", 32).default("viewer") // admin, translator, developer, viewer
    val oauthProvider = varchar("oauth_provider", 32).nullable()
    val oauthId = varchar("oauth_id", 255).nullable()
    val passwordHash = varchar("password_hash", 60)
    val isAdmin = bool("is_admin").default(false)
    val createdAt = long("created_at")
}

@OptIn(ExperimentalTime::class)
fun UserDb.insertUser(
    username: String,
    password: String,
    isAdmin: Boolean = false
): Int {
    val hash = BCrypt.withDefaults().hashToString(12, password.toCharArray())
    return insertAndGetId {
        it[this.username] = username
        it[this.passwordHash] = hash
        it[this.isAdmin] = isAdmin
        if (isAdmin) it[this.role] = "admin"
        it[this.createdAt] = Clock.System.now().toEpochMilliseconds()
    }.value
}

@OptIn(ExperimentalTime::class)
fun UserDb.insertUserFull(
    password: String,
    user: User
): Int {
    val hash = BCrypt.withDefaults().hashToString(12, password.toCharArray())
    return insertAndGetId {
        it[this.username] = user.username
        it[this.email] = user.email
        it[this.firstName] = user.firstName
        it[this.lastName] = user.lastName
        it[this.role] = user.role
        it[this.passwordHash] = hash
        it[this.isAdmin] = user.isAdmin
        it[this.createdAt] = Clock.System.now().toEpochMilliseconds()
    }.value
}

@OptIn(ExperimentalTime::class)
fun UserDb.insertOAuthUser(
    provider: String,
    oauthId: String,
    user: User
): Int {
    val randomPass = (provider + ":" + oauthId).toCharArray()
    val hash = BCrypt.withDefaults().hashToString(12, randomPass)
    return insertAndGetId {
        it[this.username] = user.username
        it[this.email] = user.email
        it[this.firstName] = user.firstName
        it[this.lastName] = user.lastName
        it[this.role] = user.role
        it[this.oauthProvider] = provider
        it[this.oauthId] = oauthId
        it[this.passwordHash] = hash
        it[this.isAdmin] = user.isAdmin
        it[this.createdAt] = Clock.System.now().toEpochMilliseconds()
    }.value
}

object LocaleDb : IntIdTable("locales") {
    val language = varchar("language", 8)
    val country = varchar("country", 8).nullable()
    val isDefault = bool("is_default").default(false)
    val isEditable = bool("is_editable").default(true)

    init {
        uniqueIndex(language, country)
    }
}

fun LocaleDb.insertLocale(locale: LocaleRequest, isDefaultFlag: Boolean = false, isEditableFlag: Boolean = true): Int = insertAndGetId {
    it[this.language] = locale.language
    it[this.country] = locale.country
    it[this.isDefault] = isDefaultFlag
    it[this.isEditable] = isEditableFlag
}.value

fun LocaleDb.insertSystemLocale(language: String, country: String?): Int = insertAndGetId {
    it[this.language] = language
    it[this.country] = country
    it[this.isDefault] = true
    it[this.isEditable] = false
}.value

object StringKeyDb : IntIdTable("string_keys") {
    val key = varchar("key", 256).uniqueIndex()
    val description = varchar("description", 1024).nullable()
    val createdAt = long("created_at")
}

object TranslationDb : IntIdTable("translations") {
    val stringId = reference("string_id", StringKeyDb)
    val localeId = reference("locale_id", LocaleDb)
    val value = text("value")
    val updatedAt = long("updated_at")

    init {
        uniqueIndex(stringId, localeId)
    }
}

object ChangeHistoryDb : IntIdTable("change_history") {
    val userId = reference("user_id", UserDb).nullable()
    val action = varchar("action", 64) // CREATE_KEY, UPDATE_TRANSLATION, IMPORT, EXPORT
    val details = text("details")
    val createdAt = long("created_at")
}

object ExportVersionDb : IntIdTable("export_versions") {
    val version = integer("version").uniqueIndex()
    val createdAt = long("created_at")
}

// Simple DAO helpers
@OptIn(ExperimentalTime::class)
fun upsertTranslation(stringId: Int, localeId: Int, value: String) {
    val now = Clock.System.now().toEpochMilliseconds()
    val existing = TranslationDb.selectAll().where { (TranslationDb.stringId eq stringId) and (TranslationDb.localeId eq localeId) }
    if (existing.empty()) {
        TranslationDb.insert {
            it[this.stringId] = stringId
            it[this.localeId] = localeId
            it[this.value] = value
            it[this.updatedAt] = now
        }
    } else {
        TranslationDb.update({ (TranslationDb.stringId eq stringId) and (TranslationDb.localeId eq localeId) }) {
            it[this.value] = value
            it[this.updatedAt] = now
        }
    }
}
