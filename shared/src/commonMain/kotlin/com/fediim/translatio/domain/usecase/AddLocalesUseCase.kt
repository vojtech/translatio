package com.fediim.translatio.domain.usecase

import com.fediim.translatio.Api
import com.fediim.translatio.shared.InternalLocale
import com.fediim.translatio.shared.LocaleRequest

class AddLocalesUseCase(val api: Api) {

    suspend operator fun invoke(locale: LocaleRequest): Result<InternalLocale> {
        return try {
            Result.success(api.addLocale(locale))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}