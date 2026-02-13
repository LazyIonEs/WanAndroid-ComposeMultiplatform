package org.lazy.wanandroid.feature.navigation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import org.lazy.wanandroid.feature.navigation.NavigationScreen

fun EntryProviderScope<NavKey>.navigationEntry() {
    entry<NavigationNavKey> {
        NavigationScreen()
    }
}