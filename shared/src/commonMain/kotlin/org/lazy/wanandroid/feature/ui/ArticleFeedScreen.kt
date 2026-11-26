package org.lazy.wanandroid.feature.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.lazy.wanandroid.collectArticleId
import org.lazy.wanandroid.core.data.repository.SessionRepository
import org.lazy.wanandroid.core.network.model.Article
import org.lazy.wanandroid.feature.feed.FeedViewModel
import org.lazy.wanandroid.feedErrorMessage
import org.lazy.wanandroid.feedKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleFeedScreen(
    section: String,
    onTopicClick: (Article) -> Unit,
    onOpenSearch: () -> Unit = {},
    onOpenCollect: () -> Unit = {},
    onOpenCoinRank: () -> Unit = {},
    onOpenProjects: () -> Unit = {},
    onOpenAuth: () -> Unit = {},
    viewModel: FeedViewModel = koinViewModel(
        key = "feed:$section",
        parameters = { parametersOf(section) },
    ),
    sessionRepository: SessionRepository = koinInject(),
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val banners by viewModel.banners.collectAsStateWithLifecycle()
    val sessionState by sessionRepository.state.collectAsStateWithLifecycle()
    val articles = viewModel.articles.collectAsLazyPagingItems()
    val isProjects = section == "projects"
    val needsCategories = isProjects && categories.items.isEmpty()
    val refresh = articles.loadState.refresh
    val append = articles.loadState.append
    val empty = articles.itemCount == 0
    val refreshError = (refresh as? LoadState.Error)?.error?.feedErrorMessage()
    val appendError = (append as? LoadState.Error)?.error?.feedErrorMessage()
    val listState = key(section, categories.selectedId) { rememberLazyListState() }
    val categoryRowState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(categories.selectedId, categories.items.size) {
        val selectedIndex = categories.items.indexOfFirst { it.id == categories.selectedId }
        if (selectedIndex >= 0) {
            categoryRowState.animateScrollToItem(selectedIndex)
        }
    }

    FeedRefreshContainer(
        isRefreshing = !empty && refresh is LoadState.Loading,
        onRefresh = {
            scope.launch { listState.scrollToItem(0) }
            if (section == "home") viewModel.refreshBanners()
            if (needsCategories) viewModel.refreshCategoriesIfNeeded() else articles.refresh()
        },
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                state = listState,
                modifier = Modifier.widthIn(max = 760.dp).fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 112.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (section == "home") {
                    if (banners.isNotEmpty()) {
                        item(key = "banner", contentType = "banner") {
                            BannerCarousel(
                                banners = banners,
                                onBannerClick = { banner ->
                                    onTopicClick(
                                        Article(
                                            id = null,
                                            title = banner.title.ifBlank { "精选内容" },
                                            link = banner.url,
                                            desc = banner.desc,
                                        )
                                    )
                                },
                            )
                        }
                    }
                    item(key = "quick-actions", contentType = "quick-actions") {
                        HomeQuickActionsRow(
                            onOpenSearch = onOpenSearch,
                            onOpenCollect = onOpenCollect,
                            onOpenCoinRank = onOpenCoinRank,
                            onOpenProjects = onOpenProjects,
                        )
                    }
                } else {
                    item(key = "editorial-header", contentType = "header") {
                        FeedEditorialHeader(section)
                    }
                }

                if (isProjects && categories.items.isNotEmpty()) {
                    item(key = "categories", contentType = "categories") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            FeedSectionHeading("探索分类", "${categories.items.size} 个方向")
                            LazyRow(
                                state = categoryRowState,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                items(categories.items, key = { it.id }) { category ->
                                    FilterChip(
                                        selected = categories.selectedId == category.id,
                                        onClick = { viewModel.selectCategory(category.id) },
                                        label = { Text(category.name) },
                                        modifier = Modifier.heightIn(min = 44.dp),
                                    )
                                }
                            }
                        }
                    }
                }

                when {
                    needsCategories -> item(key = "categories-state") {
                        if (categories.loading) {
                            FeedLoadingIndicator()
                        } else {
                            FeedPlaceholder(
                                modifier = Modifier.fillMaxWidth().heightIn(min = 300.dp),
                                loading = false,
                                title = if (categories.error != null) "分类加载失败" else "暂无项目分类",
                                message = categories.error ?: "稍后再来发现好项目",
                                onRetry = categories.error?.let { { viewModel.retryCategories() } },
                            )
                        }
                    }
                    empty -> item(key = "feed-state") {
                        val error = refreshError ?: appendError
                        if (refresh is LoadState.Loading || append is LoadState.Loading) {
                            FeedLoadingIndicator()
                        } else {
                            FeedPlaceholder(
                                modifier = Modifier.fillMaxWidth().heightIn(min = 300.dp),
                                loading = false,
                                title = if (error != null) "内容加载失败" else "暂无内容",
                                message = error ?: if (isProjects) "这个分类还没有项目，试试其他分类或稍后再来" else "暂时没有文章，稍后再来看看",
                                onRetry = error?.let { { articles.retry() } },
                            )
                        }
                    }
                    else -> {
                        if (refreshError != null) item(key = "refresh-error") {
                            FeedError(message = refreshError, onRetry = { articles.retry() })
                        }
                        item(key = "section-heading", contentType = "heading") {
                            FeedSectionHeading(
                                when (section) {
                                    "plaza" -> "最新分享"
                                    "projects" -> categories.items.firstOrNull { it.id == categories.selectedId }?.name ?: "项目列表"
                                    else -> "精选与新知"
                                },
                                "已载入 ${articles.itemCount} 篇",
                            )
                        }
                        items(count = articles.itemCount, key = articles.itemKey { it.feedKey }) { index ->
                            articles[index]?.let { item ->
                                val targetId = item.collectArticleId
                                val isCollected = (targetId != null && targetId in sessionState.collectedIds) ||
                                    (item.collect == true && sessionState.isLoggedIn)
                                ArticleCard(
                                    article = item,
                                    isProject = isProjects,
                                    featured = section == "home" && item.pinned,
                                    isCollected = isCollected,
                                    onTopicClick = onTopicClick,
                                    onToggleCollect = { clicked ->
                                        val id = clicked.collectArticleId ?: return@ArticleCard
                                        scope.launch {
                                            if (!sessionRepository.toggleCollect(id)) {
                                                onOpenAuth()
                                            }
                                        }
                                    },
                                )
                            }
                        }
                        item(key = "pagination") {
                            when {
                                appendError != null -> FeedError(message = "加载更多失败：$appendError", onRetry = { articles.retry() })
                                append is LoadState.Loading -> Row(
                                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                                    Text("正在加载更多", style = MaterialTheme.typography.bodySmall)
                                }
                                append.endOfPaginationReached -> Text(
                                    "已加载全部内容",
                                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun FeedPlaceholder(
    title: String,
    message: String,
    loading: Boolean,
    onRetry: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (loading) {
            ContainedLoadingIndicator()
        } else {
            val icon: ImageVector = if (onRetry != null) Icons.Rounded.CloudOff else Icons.Rounded.Inbox
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            if (onRetry != null) OutlinedButton(onClick = onRetry) { Text("重新加载") }
        }
    }
}

@Composable
internal fun FeedError(message: String, onRetry: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        shape = MaterialTheme.shapes.medium,
    ) {
        ListItem(
            headlineContent = { Text(message, style = MaterialTheme.typography.bodyMedium) },
            trailingContent = { TextButton(onClick = onRetry) { Text("重试") } },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        )
    }
}
