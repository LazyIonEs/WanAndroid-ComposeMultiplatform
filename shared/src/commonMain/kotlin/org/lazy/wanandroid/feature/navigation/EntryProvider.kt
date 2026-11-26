package org.lazy.wanandroid.feature.navigation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import org.lazy.wanandroid.feature.article.ArticleScreen
import org.lazy.wanandroid.feature.auth.AuthScreen
import org.lazy.wanandroid.feature.coin.CoinRankScreen
import org.lazy.wanandroid.feature.collect.CollectScreen
import org.lazy.wanandroid.feature.home.HomeScreen
import org.lazy.wanandroid.feature.plaza.PlazaScreen
import org.lazy.wanandroid.feature.profile.ProfileScreen
import org.lazy.wanandroid.feature.project.ProjectScreen
import org.lazy.wanandroid.feature.search.SearchScreen
import org.lazy.wanandroid.feature.settings.SettingsScreen
import org.lazy.wanandroid.feature.ui.ArticleDetailPlaceholder
import org.lazy.wanandroid.navigation.LocalNavigator

@OptIn(KoinExperimentalAPI::class, ExperimentalMaterial3AdaptiveApi::class)
val entryModule = module {
    navigation<HomeNavKey>(
        metadata = ListDetailSceneStrategy.listPane(detailPlaceholder = { ArticleDetailPlaceholder() })
    ) {
        val navigator = LocalNavigator.current
        HomeScreen(
            onTopicClick = { article -> navigator.navigate(ArticleNavKey(article = article)) },
            onOpenSearch = { navigator.navigate(SearchNavKey) },
            onOpenCollect = { navigator.navigate(CollectNavKey) },
            onOpenCoinRank = { navigator.navigate(CoinRankNavKey(0)) },
            onOpenProjects = { navigator.navigate(ProjectNavKey) },
            onOpenAuth = { navigator.navigate(AuthNavKey) },
        )
    }

    navigation<PlazaNavKey>(
        metadata = ListDetailSceneStrategy.listPane(detailPlaceholder = { ArticleDetailPlaceholder() })
    ) {
        val navigator = LocalNavigator.current
        PlazaScreen(
            onTopicClick = { article -> navigator.navigate(ArticleNavKey(article = article)) },
            onOpenAuth = { navigator.navigate(AuthNavKey) },
        )
    }

    navigation<ProjectNavKey>(
        metadata = ListDetailSceneStrategy.listPane(detailPlaceholder = { ArticleDetailPlaceholder() })
    ) {
        val navigator = LocalNavigator.current
        ProjectScreen(
            onTopicClick = { article -> navigator.navigate(ArticleNavKey(article = article)) },
            onOpenAuth = { navigator.navigate(AuthNavKey) },
        )
    }

    navigation<SearchNavKey>(
        metadata = ListDetailSceneStrategy.listPane(detailPlaceholder = { ArticleDetailPlaceholder() })
    ) {
        val navigator = LocalNavigator.current
        SearchScreen(
            onBack = navigator::goBack,
            onTopicClick = { article -> navigator.navigate(ArticleNavKey(article = article)) },
            onOpenAuth = { navigator.navigate(AuthNavKey) },
        )
    }

    navigation<CollectNavKey>(
        metadata = ListDetailSceneStrategy.listPane(detailPlaceholder = { ArticleDetailPlaceholder() })
    ) {
        val navigator = LocalNavigator.current
        CollectScreen(
            onTopicClick = { article -> navigator.navigate(ArticleNavKey(article = article)) },
            onOpenAuth = { navigator.navigate(AuthNavKey) },
        )
    }

    navigation<ProfileNavKey>(
        metadata = ListDetailSceneStrategy.extraPane()
    ) {
        val navigator = LocalNavigator.current
        ProfileScreen(
            onOpenAuth = { navigator.navigate(AuthNavKey) },
            onOpenCollect = { navigator.navigate(CollectNavKey) },
            onOpenCoinRank = { tab -> navigator.navigate(CoinRankNavKey(initialTab = tab)) },
            onOpenSettings = { navigator.navigate(SettingsNavKey) },
        )
    }

    navigation<AuthNavKey>(
        metadata = ListDetailSceneStrategy.extraPane()
    ) {
        val navigator = LocalNavigator.current
        AuthScreen(onAuthSuccess = navigator::goBack)
    }

    navigation<CoinRankNavKey>(
        metadata = ListDetailSceneStrategy.extraPane()
    ) { route ->
        val navigator = LocalNavigator.current
        CoinRankScreen(
            initialTab = route.initialTab,
            onOpenAuth = { navigator.navigate(AuthNavKey) },
        )
    }

    navigation<SettingsNavKey>(
        metadata = ListDetailSceneStrategy.extraPane()
    ) {
        SettingsScreen()
    }

    navigation<ArticleNavKey>(
        metadata = ListDetailSceneStrategy.detailPane()
    ) { route ->
        val navigator = LocalNavigator.current
        ArticleScreen(
            article = route.article,
            onBack = navigator::goBack,
            onOpenAuth = { navigator.navigate(AuthNavKey) },
        )
    }
}
