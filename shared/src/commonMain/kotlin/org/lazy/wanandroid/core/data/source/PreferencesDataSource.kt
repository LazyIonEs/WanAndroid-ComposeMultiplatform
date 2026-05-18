package org.lazy.wanandroid.core.data.source

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.ExperimentalSerializationApi
import org.koin.core.annotation.Provided
import org.lazy.wanandroid.core.data.model.DarkThemeConfig

@OptIn(ExperimentalSerializationApi::class, ExperimentalSettingsApi::class)
class PreferencesDataSource(@Provided settings: ObservableSettings) {

    private val flowSettings: FlowSettings = settings.toFlowSettings()

    companion object {
        private const val DARK_THEME_CONFIG = "dark_theme_config"
        private val DEFAULT_THEME_CONFIG = DarkThemeConfig.FOLLOW_SYSTEM
    }

    val darkThemeConfig: Flow<DarkThemeConfig> =
        flowSettings.getStringFlow(DARK_THEME_CONFIG, DEFAULT_THEME_CONFIG.name)
            .map { themeConfig ->
                when (themeConfig) {
                    DarkThemeConfig.FOLLOW_SYSTEM.name -> DarkThemeConfig.FOLLOW_SYSTEM
                    DarkThemeConfig.LIGHT.name -> DarkThemeConfig.LIGHT
                    DarkThemeConfig.DARK.name -> DarkThemeConfig.DARK
                    else -> DEFAULT_THEME_CONFIG
                }
            }

    suspend fun saveThemeConfig(darkThemeConfig: DarkThemeConfig) {
        flowSettings.putString(DARK_THEME_CONFIG, darkThemeConfig.name)
    }
}