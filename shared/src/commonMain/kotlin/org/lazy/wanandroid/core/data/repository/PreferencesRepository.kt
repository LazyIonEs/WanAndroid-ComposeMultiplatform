package org.lazy.wanandroid.core.data.repository

import kotlinx.coroutines.flow.Flow
import org.lazy.wanandroid.core.data.model.DarkThemeConfig
import org.lazy.wanandroid.core.data.source.PreferencesDataSource


class PreferencesRepository(private val dataSource: PreferencesDataSource) {

    val darkThemeConfig: Flow<DarkThemeConfig> = dataSource.darkThemeConfig

    suspend fun saveThemeConfig(darkThemeConfig: DarkThemeConfig) {
        dataSource.saveThemeConfig(darkThemeConfig)
    }
}