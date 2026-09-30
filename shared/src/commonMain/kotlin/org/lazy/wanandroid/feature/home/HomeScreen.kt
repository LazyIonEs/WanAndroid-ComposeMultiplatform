package org.lazy.wanandroid.feature.home

import androidx.compose.runtime.Composable
import org.lazy.wanandroid.core.network.model.Article
import org.lazy.wanandroid.feature.ui.ArticleFeedScreen
import org.lazy.wanandroid.feature.navigation.PlazaNavKey
import org.lazy.wanandroid.feature.navigation.ProjectNavKey
import org.lazy.wanandroid.navigation.LocalNavigator

@Composable
fun HomeScreen(onTopicClick: (Article) -> Unit) {
    val navigator = LocalNavigator.current
    ArticleFeedScreen(section = "home", onTopicClick = onTopicClick, onExplore = {
        navigator.navigate(if (it == "projects") ProjectNavKey else PlazaNavKey)
    })
}
