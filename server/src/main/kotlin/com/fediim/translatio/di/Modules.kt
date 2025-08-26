package com.fediim.translatio.di

import com.fediim.translatio.config.ApiConfig
import com.fediim.translatio.Exporters
import com.fediim.translatio.Importers
import com.fediim.translatio.application.AuthService
import com.fediim.translatio.application.LocaleService
import com.fediim.translatio.domain.port.LocalesRepository
import com.fediim.translatio.domain.port.UsersRepository
import com.fediim.translatio.infrastructure.ExposedLocalesRepository
import com.fediim.translatio.infrastructure.ExposedUsersRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import org.koin.dsl.module

object AppModules {
    val module = module {
        single {
            HttpClient(CIO) {
                install(ContentNegotiation) { json() }
            }
        }

        single<ApiConfig> { ApiConfig() }

        // Utility singletons
        single { Exporters }
        single { Importers }

        // Repositories (infrastructure adapters)
        single<UsersRepository> { ExposedUsersRepository() }
        single<LocalesRepository> { ExposedLocalesRepository() }

        // Application services
        single { AuthService(get()) }
        single { LocaleService(get()) }
    }
}
