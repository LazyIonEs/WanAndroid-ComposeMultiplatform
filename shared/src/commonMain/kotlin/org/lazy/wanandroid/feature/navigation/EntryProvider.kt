package org.lazy.wanandroid.feature.navigation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import org.lazy.wanandroid.feature.article.ArticleScreen
import org.lazy.wanandroid.feature.home.HomeScreen
import org.lazy.wanandroid.feature.plaza.PlazaScreen
import org.lazy.wanandroid.feature.project.ProjectScreen
import org.lazy.wanandroid.feature.settings.SettingsScreen
import org.lazy.wanandroid.navigation.LocalNavigator

@OptIn(KoinExperimentalAPI::class, ExperimentalMaterial3AdaptiveApi::class)
val entryModule = module {
    navigation<HomeNavKey>(
        metadata = ListDetailSceneStrategy.listPane()
    ) {
        val navigator = LocalNavigator.current
        HomeScreen(onTopicClick = { article ->
            navigator.navigate(ArticleNavKey(article = article))
        })
    }

    navigation<PlazaNavKey> {
        PlazaScreen()
    }

    navigation<ProjectNavKey> {
        ProjectScreen()
    }

    navigation<SettingsNavKey> {
        SettingsScreen()
    }

    navigation<ArticleNavKey>(
        metadata = ListDetailSceneStrategy.detailPane()
    ) { route ->
        ArticleScreen(route.article)
    }
}