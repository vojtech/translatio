package com.fediim.translatio

import com.fediim.translatio.locales.viewmodel.LocalesViewModel
import com.fediim.translatio.localisation.LocalisationViewModel
import com.fediim.translatio.domain.usecase.AddLocalesUseCase
import com.fediim.translatio.domain.usecase.GetLocalesUseCase
import com.fediim.translatio.domain.usecase.LoginUseCase
import com.fediim.translatio.domain.usecase.LogoutUseCase
import com.fediim.translatio.domain.usecase.RegisterUseCase
import com.fediim.translatio.login.viewmodel.LoginViewModel
import com.fediim.translatio.register.viewmodel.RegisterViewModel
import com.fediim.translatio.util.DebugLogger
import com.fediim.translatio.util.Logger
import org.koin.core.Koin
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

fun initApp(logger: Logger) {
    initKoin(logger)
}

fun initKoin(logger: Logger): Koin {
    return startKoin {
        val appModule = module {
            singleOf(::Api)
            single<Logger> { DebugLogger(logger) }
        }

        val useCaseModule = module {
            singleOf(::LoginUseCase)
            singleOf(::RegisterUseCase)
            singleOf(::LogoutUseCase)
            singleOf(::AddLocalesUseCase)
            singleOf(::GetLocalesUseCase)
        }

        val viewModelsModule = module {
            viewModelOf(::LoginViewModel)
            viewModelOf(::RegisterViewModel)
            viewModelOf(::LocalesViewModel)
            viewModelOf(::LocalisationViewModel)
        }
        modules(appModule, useCaseModule, viewModelsModule)
    }.koin
}

