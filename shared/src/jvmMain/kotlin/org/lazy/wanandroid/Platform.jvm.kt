package org.lazy.wanandroid

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.PreferencesSettings
import org.jetbrains.compose.resources.FontResource
import org.koin.dsl.module

class JVMPlatform : Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual fun getPlatformFontResource(): FontResource? = null

actual val platformModule = module {
    single<ObservableSettings> { PreferencesSettings.Factory().create(SETTINGS_NAME) }
}
