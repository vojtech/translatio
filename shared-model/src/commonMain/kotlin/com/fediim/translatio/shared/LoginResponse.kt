package com.fediim.translatio.shared

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(val userId: Int, val username: String)