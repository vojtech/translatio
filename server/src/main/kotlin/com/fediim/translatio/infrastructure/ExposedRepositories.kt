package com.fediim.translatio.infrastructure

import at.favre.lib.crypto.bcrypt.BCrypt
import com.fediim.translatio.db.LocaleDb
import com.fediim.translatio.db.UserDb
import com.fediim.translatio.db.StringKeyDb
import com.fediim.translatio.db.upsertTranslation
import com.fediim.translatio.db.insertUserFull
import com.fediim.translatio.db.insertLocale
import com.fediim.translatio.db.insertOAuthUser
import com.fediim.translatio.domain.port.LocalesRepository
import com.fediim.translatio.domain.port.UsersRepository
import com.fediim.translatio.shared.InternalLocale
import com.fediim.translatio.shared.LocaleRequest
import com.fediim.translatio.shared.User
import com.fediim.translatio.shared.UserRecord
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class ExposedUsersRepository : UsersRepository {
    override fun findByUsername(username: String): UserRecord? = transaction {
        UserDb.selectAll().where { UserDb.username eq username }.singleOrNull()?.let { row ->
            UserRecord(
                id = row[UserDb.id].value,
                username = row[UserDb.username],
                passwordHash = row[UserDb.passwordHash],
                role = row[UserDb.role]
            )
        }
    }

    override fun usernameExists(username: String): Boolean = transaction {
        UserDb.selectAll().where { UserDb.username eq username }.empty().not()
    }

    override fun insertUserFull(
        user: User,
        password: String
    ): Int = transaction {
        UserDb.insertUserFull(password, user)
    }

    override fun verifyPasswordHash(plain: String, hash: String): Boolean =
        BCrypt.verifyer().verify(plain.toCharArray(), hash).verified

    override fun findUserIdByOAuth(provider: String, oauthId: String): Int? = transaction {
        UserDb.selectAll().where { (UserDb.oauthProvider eq provider) and (UserDb.oauthId eq oauthId) }
            .singleOrNull()?.get(UserDb.id)?.value
    }

    override fun insertOAuthUser(
        provider: String,
        oauthId: String,
        user: User
    ): Int = transaction {
        UserDb.insertOAuthUser(provider, oauthId, user)
    }
}

class ExposedLocalesRepository : LocalesRepository {
    override fun list(): List<InternalLocale> = transaction {
        LocaleDb.selectAll().map { row ->
            InternalLocale(
                id = row[LocaleDb.id].value,
                language = row[LocaleDb.language],
                country = row[LocaleDb.country],
                isDefault = row[LocaleDb.isDefault],
                editable = row[LocaleDb.isEditable]
            )
        }
    }

    override fun upsert(locale: LocaleRequest): Int = transaction {
        val existing = LocaleDb.selectAll().where { (LocaleDb.language eq locale.language) and (LocaleDb.country eq locale.country) }.singleOrNull()
        val id = existing?.get(LocaleDb.id)?.value ?: LocaleDb.insertLocale(locale, isDefaultFlag = false, isEditableFlag = true)
        // If we created a new locale, ensure translations exist for all existing strings
        if (existing == null) {
            StringKeyDb.selectAll().forEach { keyRow ->
                val stringId = keyRow[StringKeyDb.id].value
                upsertTranslation(stringId, id, "")
            }
        }
        id
    }
}
