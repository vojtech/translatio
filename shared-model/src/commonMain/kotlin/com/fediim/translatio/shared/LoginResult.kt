package com.fediim.translatio.shared

import kotlinx.serialization.Serializable

@Serializable
data class LoginResult(val userId: Int, val username: String)
