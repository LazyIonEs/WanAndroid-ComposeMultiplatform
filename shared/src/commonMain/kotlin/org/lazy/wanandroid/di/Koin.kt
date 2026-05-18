package org.lazy.wanandroid.di

import com.russhwolf.settings.ExperimentalSettingsApi
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.includes
import org.koin.dsl.module
import org.koin.plugin.module.dsl.create
import org.koin.plugin.module.dsl.single
import org.koin.plugin.module.dsl.viewModel
import org.lazy.wanandroid.AppViewModel
import org.lazy.wanandroid.core.data.repository.HomeRepository
import org.lazy.wanandroid.core.data.repository.PreferencesRepository
import org.lazy.wanandroid.core.data.source.NetworkDataSource
import org.lazy.wanandroid.core.data.source.PreferencesDataSource
import org.lazy.wanandroid.feature.home.HomeViewModel
import org.lazy.wanandroid.feature.navigation.entryModule
import org.lazy.wanandroid.feature.settings.SettingsViewModel
import org.lazy.wanandroid.httpClient
import org.lazy.wanandroid.platformModule

val viewModelModule = module {
    viewModel<AppViewModel>()
    viewModel<HomeViewModel>()
    viewModel<SettingsViewModel>()
}

@OptIn(ExperimentalSettingsApi::class)
val dataModule = module {
    single<HttpClient> { create(::buildClient) }
    single<NetworkDataSource>()

    single<PreferencesDataSource>()

    single<HomeRepository>()
    single<PreferencesRepository>()
}

private fun buildClient(): HttpClient {
    return httpClient {
        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = 1)
            exponentialDelay()
        }
        install(HttpTimeout) {
            socketTimeoutMillis = 30000
        }
        install(Logging) {
            level = LogLevel.ALL
            // logger = Logger.SIMPLE
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
                isLenient = true
                allowSpecialFloatingPointValues = true
                allowStructuredMapKeys = true
                prettyPrint = false
                useArrayPolymorphism = false
            })
        }
    }
}

val navigationModule = module {
    includes(entryModule)
}

val appModule = module {
    includes(platformModule, dataModule, navigationModule, viewModelModule)
}

fun initKoin(configuration: KoinAppDeclaration? = null) {
    startKoin {
        includes(configuration)
        modules(appModule)
        printLogger(Level.DEBUG)
    }
}