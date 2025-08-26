package com.fediim.translatio.application

import com.fediim.translatio.db.UserDb
import com.fediim.translatio.db.insertOAuthUser
import com.fediim.translatio.domain.port.OAuthPort
import com.fediim.translatio.domain.port.UsersRepository
import com.fediim.translatio.shared.LoginResult
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class OAuthAuthService(private val users: UsersRepository, private val oauth: OAuthPort) {

    suspend fun loginWithProvider(provider: String, accessToken: String): LoginResult {
        val u = oauth.fetchUser(provider, accessToken)
        val userId = transaction {
            val existing =
                UserDb.selectAll().where { (UserDb.oauthProvider eq u.provider) and (UserDb.oauthId eq u.oauthId) }.singleOrNull()
            if (existing != null) existing[UserDb.id].value else UserDb.insertOAuthUser(u.provider, u.oauthId, u.user)
        }
        return LoginResult(userId, u.user.username)
    }
}
