package org.lazy.wanandroid.feature.home.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import org.lazy.wanandroid.feature.home.HomeScreen

fun EntryProviderScope<NavKey>.homeEntry() {
    entry<HomeNavKey> {
        HomeScreen()
    }
}