package org.lazy.wanandroid.feature.plaza

import androidx.compose.runtime.Composable
import org.lazy.wanandroid.core.network.model.Article
import org.lazy.wanandroid.feature.ui.ArticleFeedScreen

@Composable
fun PlazaScreen(onTopicClick: (Article) -> Unit) {
    ArticleFeedScreen(section = "plaza", onTopicClick = onTopicClick)
}
