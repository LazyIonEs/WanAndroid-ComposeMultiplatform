package org.lazy.wanandroid.feature.settings.navigation

import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import org.lazy.wanandroid.feature.settings.SettingsScreen

@OptIn(KoinExperimentalAPI::class)
val settingsEntry = module {
    navigation<SettingsNavKey> { route ->
        SettingsScreen()
    }
}