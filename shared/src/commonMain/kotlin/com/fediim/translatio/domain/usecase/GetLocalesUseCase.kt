package com.fediim.translatio.domain.usecase

import com.fediim.translatio.Api
import com.fediim.translatio.shared.InternalLocale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetLocalesUseCase(val api: Api) {

    operator fun invoke(): Flow<Result<List<InternalLocale>>> = flow {
         try {
            emit(Result.success(api.locales()))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}