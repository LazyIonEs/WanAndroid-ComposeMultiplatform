package org.lazy.wanandroid

import android.os.Build
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SharedPreferencesSettings
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp
import org.jetbrains.compose.resources.FontResource
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun getBaseUrl(): String = BASE_URL

actual fun httpClient(config: HttpClientConfig<*>.() -> Unit) = HttpClient(OkHttp) {
    config(this)

    engine {
        config {
            retryOnConnectionFailure(true)
            connectTimeout(30, TimeUnit.SECONDS)
            readTimeout(30, TimeUnit.SECONDS)
            writeTimeout(30, TimeUnit.SECONDS)
        }
    }
}

actual fun getPlatformFontResource(): FontResource? = null

actual val platformModule = module {
    single<ObservableSettings> { SharedPreferencesSettings.Factory(get()).create(SETTINGS_NAME) }
}