package org.lazy.wanandroid.feature.article

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.nucleusframework.webview.web.WebView
import dev.nucleusframework.webview.web.rememberWebViewNavigator
import dev.nucleusframework.webview.web.rememberWebViewState
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.lazy.wanandroid.collectArticleId
import org.lazy.wanandroid.core.data.repository.SessionRepository
import org.lazy.wanandroid.core.network.model.Article
import org.lazy.wanandroid.theme.WanAccents
import org.lazy.wanandroid.utils.WanAndroidRequestInterceptor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleScreen(
    article: Article,
    onBack: () -> Unit = {},
    onOpenAuth: () -> Unit = {},
    sessionRepository: SessionRepository = koinInject(),
) {
    val state = rememberWebViewState(article.link)
    val navigator = rememberWebViewNavigator(requestInterceptor = WanAndroidRequestInterceptor)
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    val session by sessionRepository.state.collectAsStateWithLifecycle()

    val collectId = article.collectArticleId
    val isCollected = (collectId != null && collectId in session.collectedIds) ||
        (article.collect == true && session.isLoggedIn)

    val hostLabel = remember(article.link) {
        article.link.removePrefix("https://").removePrefix("http://").substringBefore("/")
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = article.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    if (hostLabel.isNotBlank()) {
                        Text(
                            text = hostLabel,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "返回")
                }
            },
            actions = {
                if (collectId != null) {
                    IconToggleButton(
                        checked = isCollected,
                        onCheckedChange = {
                            scope.launch {
                                if (!sessionRepository.toggleCollect(collectId)) {
                                    onOpenAuth()
                                }
                            }
                        },
                        colors = IconButtonDefaults.iconToggleButtonColors(
                            checkedContentColor = WanAccents.HeartRed,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    ) {
                        Icon(
                            imageVector = if (isCollected) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = if (isCollected) "取消收藏" else "收藏文章",
                        )
                    }
                }
                IconButton(onClick = { navigator.reload() }) {
                    Icon(Icons.Rounded.Refresh, contentDescription = "刷新网页")
                }
                IconButton(onClick = { uriHandler.openUri(article.link) }) {
                    Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = "浏览器打开")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
            windowInsets = WindowInsets(0),
        )

        AnimatedVisibility(visible = state.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            WebView(
                state = state,
                modifier = Modifier.fillMaxSize(),
                navigator = navigator,
            )
        }
    }
}
