package org.lazy.wanandroid.feature.project

import androidx.compose.runtime.Composable
import org.lazy.wanandroid.core.network.model.Article
import org.lazy.wanandroid.feature.ui.ArticleFeedScreen

@Composable
fun ProjectScreen(onTopicClick: (Article) -> Unit) {
    ArticleFeedScreen(section = "projects", onTopicClick = onTopicClick)
}
