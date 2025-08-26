package com.fediim.translatio.domain.usecase

import com.fediim.translatio.Api
import com.fediim.translatio.shared.LoginRequest
import com.fediim.translatio.shared.LoginResponse

class LoginUseCase(val api: Api) {

    suspend operator fun invoke(loginRequest: LoginRequest): Result<LoginResponse> {
        return try {
            Result.success(api.login(loginRequest))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}