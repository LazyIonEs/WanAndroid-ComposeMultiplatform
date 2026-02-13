package org.lazy.wanandroid.feature.project.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import org.lazy.wanandroid.feature.project.ProjectScreen

fun EntryProviderScope<NavKey>.projectEntry() {
    entry<ProjectNavKey> {
        ProjectScreen()
    }
}