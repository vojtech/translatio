package com.fediim.translatio.domain.port

import com.fediim.translatio.shared.User
import com.fediim.translatio.shared.UserRecord


interface UsersRepository {
    fun findByUsername(username: String): UserRecord?
    fun usernameExists(username: String): Boolean
    fun insertUserFull(user: User, password: String): Int
    fun verifyPasswordHash(plain: String, hash: String): Boolean
    fun findUserIdByOAuth(provider: String, oauthId: String): Int?
    fun insertOAuthUser(provider: String, oauthId: String, user: User): Int
}
