package org.lazy.wanandroid.di

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.includes
import org.koin.dsl.module
import org.koin.plugin.module.dsl.create
import org.lazy.wanandroid.core.data.repository.HomeRepository
import org.lazy.wanandroid.core.network.NetworkDataSource
import org.lazy.wanandroid.feature.home.HomeViewModel
import org.lazy.wanandroid.feature.home.navigation.homeEntry
import org.lazy.wanandroid.feature.navigation.navigation.navigationEntry
import org.lazy.wanandroid.feature.project.navigation.projectEntry
import org.lazy.wanandroid.feature.settings.navigation.settingsEntry
import org.lazy.wanandroid.feature.wechat.navigation.weChatEntry
import org.lazy.wanandroid.httpClient

val viewModelModule = module {
    viewModel { HomeViewModel(get()) }
}

val dataModule = module {
    single { create(::buildClient) }
    single { NetworkDataSource(get()) }
    single { HomeRepository(get()) }
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

@OptIn(KoinExperimentalAPI::class)
val navigationModule = module {
    includes(homeEntry, navigationEntry, projectEntry, settingsEntry, weChatEntry)
}

val appModule = module {
    includes(dataModule, viewModelModule, navigationModule)
}

fun initKoin(configuration: KoinAppDeclaration? = null) {
    startKoin {
        includes(configuration)
        modules(appModule)
        printLogger(Level.DEBUG)
    }
}