package org.lazy.wanandroid.feature.navigation.navigation

import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import org.lazy.wanandroid.feature.navigation.NavigationScreen

@OptIn(KoinExperimentalAPI::class)
val navigationEntry = module {
    navigation<NavigationNavKey> { route ->
        NavigationScreen()
    }
}