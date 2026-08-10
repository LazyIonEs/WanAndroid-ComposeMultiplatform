package org.lazy.wanandroid.feature.article

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.nucleusframework.webview.web.WebView
import dev.nucleusframework.webview.web.rememberWebViewNavigator
import dev.nucleusframework.webview.web.rememberWebViewState
import org.lazy.wanandroid.core.network.model.Article
import org.lazy.wanandroid.utils.WanAndroidRequestInterceptor

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ArticleScreen(article: Article) {
    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        val state = rememberWebViewState(article.link)
        val navigator = rememberWebViewNavigator(requestInterceptor = WanAndroidRequestInterceptor)
        WebView(
            state = state,
            modifier = Modifier.fillMaxSize(),
            navigator = navigator
        ) {
            AnimatedVisibility(
                visible = state.isLoading,
                modifier = Modifier.align(Alignment.Center)
            ) {
                ContainedLoadingIndicator()
            }
        }
    }
}