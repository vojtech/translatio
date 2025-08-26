package com.fediim.translatio.domain.usecase

import com.fediim.translatio.Api
import com.fediim.translatio.shared.RegisterRequest
import com.fediim.translatio.shared.RegisterResponse

class RegisterUseCase(val api: Api) {

    suspend operator fun invoke(request: RegisterRequest): Result<RegisterResponse> {
        return try {
            Result.success(api.register(request))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}