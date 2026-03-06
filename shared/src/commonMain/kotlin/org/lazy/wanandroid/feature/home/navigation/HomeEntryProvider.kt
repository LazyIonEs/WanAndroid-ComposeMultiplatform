package org.lazy.wanandroid.feature.home.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import org.lazy.wanandroid.feature.home.HomeScreen
import org.lazy.wanandroid.feature.home.TestScreen
import org.lazy.wanandroid.navigation.LocalNavigator

@OptIn(KoinExperimentalAPI::class)
val homeEntry = module {
    navigation<HomeNavKey> { route ->
        val navigator = LocalNavigator.current
        HomeScreen(onTopicClick = {
            navigator.navigate(NavigationTestNavKey)
        })
    }
    navigation<NavigationTestNavKey> {
        TestScreen()
    }
}

val homeSerializersModule = SerializersModule {
    polymorphic(NavKey::class) {
        subclass(HomeNavKey::class)
        subclass(NavigationTestNavKey::class)
    }
}