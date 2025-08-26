package com.fediim.translatio.domain.usecase

import com.fediim.translatio.Api

class LogoutUseCase(val api: Api) {

    suspend operator fun invoke(): Result<Unit> {
        return try {
            Result.success(api.logout())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}