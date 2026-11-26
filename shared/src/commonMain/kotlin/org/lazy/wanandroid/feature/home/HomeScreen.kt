package org.lazy.wanandroid.feature.home

import androidx.compose.runtime.Composable
import org.lazy.wanandroid.core.network.model.Article
import org.lazy.wanandroid.feature.ui.ArticleFeedScreen

@Composable
fun HomeScreen(
    onTopicClick: (Article) -> Unit,
    onOpenSearch: () -> Unit = {},
    onOpenCollect: () -> Unit = {},
    onOpenCoinRank: () -> Unit = {},
    onOpenProjects: () -> Unit = {},
    onOpenAuth: () -> Unit = {},
) {
    ArticleFeedScreen(
        section = "home",
        onTopicClick = onTopicClick,
        onOpenSearch = onOpenSearch,
        onOpenCollect = onOpenCollect,
        onOpenCoinRank = onOpenCoinRank,
        onOpenProjects = onOpenProjects,
        onOpenAuth = onOpenAuth,
    )
}
