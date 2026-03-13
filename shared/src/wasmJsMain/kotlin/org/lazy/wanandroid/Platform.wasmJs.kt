package org.lazy.wanandroid

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.StorageSettings
import com.russhwolf.settings.observable.makeObservable
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.js.Js
import kotlinx.browser.window
import org.jetbrains.compose.resources.FontResource
import org.koin.dsl.module
import wanandroid.shared.generated.resources.NotoSansSC_Bold
import wanandroid.shared.generated.resources.Res

class WasmPlatform : Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

actual fun getBaseUrl(): String = CORS_URL

actual fun httpClient(config: HttpClientConfig<*>.() -> Unit) = HttpClient(Js) {
    config(this)
}

actual fun getPlatformFontResource(): FontResource? = Res.font.NotoSansSC_Bold

@OptIn(ExperimentalSettingsApi::class)
actual val platformModule = module {
    single<ObservableSettings> { StorageSettings(window.localStorage).makeObservable() }
}
