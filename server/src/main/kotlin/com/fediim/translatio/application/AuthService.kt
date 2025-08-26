package com.fediim.translatio.application

import com.fediim.translatio.domain.port.UsersRepository
import com.fediim.translatio.shared.LoginResult
import com.fediim.translatio.shared.RegisterRequest
import com.fediim.translatio.shared.Role
import com.fediim.translatio.shared.User

class AuthService(private val users: UsersRepository) {

    fun login(username: String, password: String): LoginResult? {
        val rec = users.findByUsername(username) ?: return null
        val ok = users.verifyPasswordHash(password, rec.passwordHash)
        return if (ok) LoginResult(rec.id, rec.username) else null
    }

    fun register(user: RegisterRequest, role: Role): Int {
        if (user.email.isBlank() || user.password.isBlank()) throw IllegalArgumentException("Username and password are required")
        if (users.usernameExists(user.email)) throw IllegalArgumentException("Username already exists")
        return users.insertUserFull(
            user = User(
                username = user.email,
                email = user.email,
                firstName = user.firstName,
                lastName = user.lastName,
                role = role.name
            ),
            password = user.password
        )
    }

    fun loginOrRegisterOAuth(
        provider: String,
        oauthId: String,
        user: User
    ): LoginResult {
        val existingId = users.findUserIdByOAuth(provider, oauthId)
        val userId = existingId ?: users.insertOAuthUser(provider = provider, oauthId = oauthId, user = user)
        return LoginResult(userId = userId, username = user.username)
    }
}
