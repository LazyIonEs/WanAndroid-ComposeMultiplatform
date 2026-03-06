package org.lazy.wanandroid.feature.project.navigation

import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import org.lazy.wanandroid.feature.project.ProjectScreen

@OptIn(KoinExperimentalAPI::class)
val projectEntry = module {
    navigation<ProjectNavKey> { route ->
        ProjectScreen()
    }
}