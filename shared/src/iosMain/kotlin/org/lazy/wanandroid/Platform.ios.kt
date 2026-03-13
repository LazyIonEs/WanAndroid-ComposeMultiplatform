package org.lazy.wanandroid

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.ObservableSettings
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.darwin.Darwin
import org.jetbrains.compose.resources.FontResource
import org.koin.dsl.module
import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun getBaseUrl(): String = BASE_URL

actual fun httpClient(config: HttpClientConfig<*>.() -> Unit) = HttpClient(Darwin) {
    config(this)

    engine {
        configureRequest {
            setAllowsCellularAccess(true)
        }
    }
}

actual fun getPlatformFontResource(): FontResource? = null

actual val platformModule = module {
    single<ObservableSettings> { NSUserDefaultsSettings.Factory().create(SETTINGS_NAME) }
}