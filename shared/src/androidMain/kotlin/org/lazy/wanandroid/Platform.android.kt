package org.lazy.wanandroid

import android.os.Build
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SharedPreferencesSettings
import org.jetbrains.compose.resources.FontResource
import org.koin.dsl.module

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun getPlatformFontResource(): FontResource? = null

actual val platformModule = module {
    single<ObservableSettings> { SharedPreferencesSettings.Factory(get()).create(SETTINGS_NAME) }
}
