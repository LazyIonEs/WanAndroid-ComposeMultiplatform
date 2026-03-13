package org.lazy.wanandroid

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.PreferencesSettings
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.apache5.Apache5
import org.jetbrains.compose.resources.FontResource
import org.koin.dsl.module

class JVMPlatform : Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual fun getBaseUrl(): String = BASE_URL

actual fun httpClient(config: HttpClientConfig<*>.() -> Unit) = HttpClient(Apache5) {
    config(this)
}

actual fun getPlatformFontResource(): FontResource? = null

actual val platformModule = module {
    single<ObservableSettings> { PreferencesSettings.Factory().create(SETTINGS_NAME) }
}