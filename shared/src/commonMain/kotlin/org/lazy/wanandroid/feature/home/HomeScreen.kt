package org.lazy.wanandroid.feature.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.window.core.layout.WindowSizeClass
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import com.materialkolor.ktx.harmonize
import org.koin.compose.viewmodel.koinViewModel
import org.lazy.wanandroid.LocalAppState
import org.lazy.wanandroid.LocalWindowAdaptiveInfo
import org.lazy.wanandroid.common.AmbientRow
import org.lazy.wanandroid.common.defaultTransition
import org.lazy.wanandroid.common.listEnterTransition
import org.lazy.wanandroid.core.network.model.Article
import org.lazy.wanandroid.feature.navigation.ArticleNavKey
import kotlin.random.Random

@Composable
fun HomeScreen(
    onTopicClick: (Article) -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val lazyPagingItems = viewModel.articleList.collectAsLazyPagingItems()
    val articleTop by viewModel.articleTop.collectAsStateWithLifecycle()
    HomeScreen(
        articleList = lazyPagingItems,
        articleTop = articleTop,
        onTopicClick = onTopicClick,
        retry = { lazyPagingItems.retry() })
}

@Composable
internal fun HomeScreen(
    articleList: LazyPagingItems<Article>,
    articleTop: List<Article>?,
    onTopicClick: (Article) -> Unit,
    retry: () -> Unit
) {
    val refreshState = articleList.loadState.refresh
    AnimatedContent(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        targetState = refreshState,
        transitionSpec = {
            if (initialState == LoadState.Loading && targetState is LoadState.NotLoading) {
                listEnterTransition()
            } else {
                defaultTransition()
            }
        },
        contentAlignment = Alignment.Center
    ) { loadState ->
        when (loadState) {
            is LoadState.Loading -> HomeScreenContainedLoading()
            is LoadState.Error -> {
                val error = loadState.error.message ?: "Unknown error"
                HomeScreenError(error, retry)
            }

            else -> HomeScreenContent(
                articleList = articleList,
                articleTop = articleTop,
                onTopicClick = onTopicClick
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun HomeScreenContent(
    articleList: LazyPagingItems<Article>,
    articleTop: List<Article>?,
    onTopicClick: (Article) -> Unit,
) {
    val appendState = articleList.loadState.append
    LazyVerticalStaggeredGrid(
        columns = rememberColumns(),
        modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
    ) {
        if (articleTop?.isNotEmpty() == true) {
            items(
                items = articleTop,
                key = { article ->
                    article.id ?: articleTop.indexOf(article)
                },
            ) { article ->
                HomeScreenItem(
                    article = article,
                    pinToTop = true,
                    onTopicClick = onTopicClick,
                    modifier = Modifier.animateItem()
                )
            }
        }

        items(
            count = articleList.itemCount,
            key = { index ->
                val article = articleList.peek(index)
                article?.id ?: index
            }
        ) { index ->
            val article = articleList[index] ?: return@items
            HomeScreenItem(
                article = article,
                pinToTop = false,
                onTopicClick = onTopicClick,
                modifier = Modifier.animateItem()
            )
        }

        if (appendState == LoadState.Loading) {
            item(key = "Loading", span = StaggeredGridItemSpan.FullLine) {
                HomeScreenAppendLoading(Modifier.animateItem())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun rememberColumns(): StaggeredGridCells {
    val windowAdaptiveInfo = LocalWindowAdaptiveInfo.current
    val currentKey = LocalAppState.current.navigationState.currentKey
    return remember(windowAdaptiveInfo, currentKey) {
        val scaffoldDirective = calculatePaneScaffoldDirective(windowAdaptiveInfo)

        val isMultiPane = scaffoldDirective.maxHorizontalPartitions > 1

        val enterSecondaryPage = currentKey is ArticleNavKey

        val isCurrentlyMultiPane = isMultiPane && enterSecondaryPage

        val windowSizeClass = windowAdaptiveInfo.windowSizeClass

        when {
            isCurrentlyMultiPane -> StaggeredGridCells.Fixed(1)
            windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXTRA_LARGE_LOWER_BOUND) ->
                StaggeredGridCells.Fixed(6)

            windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND) ->
                StaggeredGridCells.Fixed(5)

            windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) ->
                StaggeredGridCells.Fixed(4)

            windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) ->
                StaggeredGridCells.Fixed(3)

            else -> StaggeredGridCells.Fixed(2)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalAnimationApi::class)
@Composable
private fun HomeScreenContainedLoading() {
    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        ContainedLoadingIndicator()
    }
}

@Composable
private fun HomeScreenError(error: String, retry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = error)
        Button(
            onClick = retry, modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("重试")
        }
    }
}

@Composable
private fun HomeScreenItem(
    article: Article,
    pinToTop: Boolean,
    onTopicClick: (Article) -> Unit,
    modifier: Modifier = Modifier
) {
    val title = article.title
    val author = article.author?.ifBlank { article.shareUser } ?: ""
    val niceDate = article.niceDate?.ifBlank { article.niceShareDate } ?: ""
    ElevatedCard(
        modifier = modifier.padding(8.dp).clickable {
            onTopicClick.invoke(article)
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            AmbientRow(
                modifier = Modifier.fillMaxWidth().height(64.dp)
            ) {
                Spacer(Modifier.size(4.dp))
                if (pinToTop) {
                    LabelText(
                        label = "置顶",
                        background = MaterialTheme.colorScheme.onPrimary,
                        textColor = MaterialTheme.colorScheme.primary
                    )
                }
                if (article.fresh == true) {
                    LabelText(
                        label = "NEW",
                        background = MaterialTheme.colorScheme.onPrimary,
                        textColor = MaterialTheme.colorScheme.primary
                    )
                }
                if (article.chapterName?.isNotBlank() == true) {
                    LabelText(
                        label = article.chapterName,
                        background = MaterialTheme.colorScheme.onSecondary,
                        textColor = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(10.dp)
            ) {

                val doc: Document = Ksoup.parse(html = title)

                Text(
                    text = doc.text(),
                    style = MaterialTheme.typography.labelLarge
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val color = MaterialTheme.colorScheme.secondaryContainer
                    val randomColor = remember {
                        color.harmonize(
                            Color.hsv(
                                hue = Random.nextFloat() * 360f,
                                saturation = 1f,
                                value = 1f
                            )
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(randomColor)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = author.getOrElse(0) { '文' }.uppercase(),
                            fontWeight = FontWeight.Black,
                            fontSize = 8.sp,
                            lineHeight = 8.sp,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }

                    Text(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 6.dp),
                        text = author,
                        maxLines = 1,
                        fontSize = 11.sp,
                        lineHeight = 11.sp,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }

                Text(
                    text = niceDate,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 10.sp,
                    lineHeight = 10.sp,
                    color = MaterialTheme.colorScheme.outline,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
private fun LabelText(label: String, background: Color, textColor: Color) {
    Box(
        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            .height(18.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 6.dp),
            text = label,
            textAlign = TextAlign.Center,
            color = textColor,
            fontSize = 9.sp,
            lineHeight = 9.sp,
            maxLines = 1,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalAnimationApi::class)
@Composable
private fun HomeScreenAppendLoading(modifier: Modifier) {
    val bottomPadding = 16.dp + FloatingToolbarDefaults.ContainerSize + 32.dp
    Box(
        modifier = modifier.fillMaxWidth().padding(top = 16.dp, bottom = bottomPadding),
        contentAlignment = Alignment.Center
    ) {
        LoadingIndicator()
    }
}