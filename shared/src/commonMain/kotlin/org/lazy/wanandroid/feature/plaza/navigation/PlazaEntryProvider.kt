package org.lazy.wanandroid.feature.plaza.navigation

import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import org.lazy.wanandroid.feature.plaza.PlazaScreen

@OptIn(KoinExperimentalAPI::class)
val plazaEntry = module {
    navigation<PlazaNavKey> { route ->
        PlazaScreen()
    }
}