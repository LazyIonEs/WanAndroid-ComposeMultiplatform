package org.lazy.wanandroid.feature.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import org.koin.compose.viewmodel.koinViewModel
import org.lazy.wanandroid.common.defaultTransition
import org.lazy.wanandroid.common.listEnterTransition
import org.lazy.wanandroid.core.network.model.Article

@Composable
fun HomeScreen(
    onTopicClick: () -> Unit, viewModel: HomeViewModel = koinViewModel()
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
    onTopicClick: () -> Unit,
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
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun HomeScreenContent(
    articleList: LazyPagingItems<Article>,
    articleTop: List<Article>?,
) {
    val appendState = articleList.loadState.append
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (articleTop?.isNotEmpty() == true) {
            items(
                items = articleTop, key = { article ->
                    article.id ?: articleTop.indexOf(article)
                }) { article ->
                val title = article.title
                val author = article.author?.ifBlank { article.shareUser } ?: ""
                val niceDate = article.niceDate?.ifBlank { article.niceShareDate } ?: ""
                HomeScreenItem(
                    title = title,
                    author = author,
                    niceDate = niceDate,
                    modifier = Modifier.animateItem()
                )
            }
        }

        items(
            count = articleList.itemCount, key = { index ->
                val article = articleList.peek(index)
                article?.id ?: index
            }) { index ->
            val article = articleList[index] ?: return@items
            val title = article.title
            val author = article.author?.ifBlank { article.shareUser } ?: ""
            val niceDate = article.niceDate?.ifBlank { article.niceShareDate } ?: ""
            HomeScreenItem(
                title = title,
                author = author,
                niceDate = niceDate,
                modifier = Modifier.animateItem()
            )
        }

        if (appendState == LoadState.Loading) {
            item(key = "Loading") {
                HomeScreenAppendLoading(Modifier.animateItem())
            }
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
    title: String,
    author: String,
    niceDate: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier.fillMaxWidth().height(128.dp).background(
                    color = Color(0XFFDCE8B3)
                )
            ) {

            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {

                val doc: Document = Ksoup.parse(html = title)

                Text(
                    text = doc.text(),
                    style = MaterialTheme.typography.titleLarge
                )

                HorizontalDivider(modifier = Modifier.padding(top = 12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = author.getOrElse(0) { '文' }.uppercase(),
                            autoSize = TextAutoSize.StepBased(),
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }

                    Text(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        text = "$author · $niceDate",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    IconButton(onClick = { }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.FavoriteBorder,
                            contentDescription = "Favorite",
                        )
                    }
                }
            }
        }
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

@Preview(showBackground = true)
@Composable
private fun HomeScreenItemPreview() {
    Column {
        HomeScreenItem(
            title = "<em class='highlight'>Compose</em> <em class='highlight'>Multiplatform</em> 1.10 Interop views 新特性：Overlay 和 Autosizing",
            author = "abc",
            niceDate = "11小时前"
        )
        HomeScreenItem(
            title = "测试标题，我很长我很长我很长我很长",
            author = "作者",
            niceDate = "11小时前"
        )
        HomeScreenItem(
            title = "测试标题，我很长我很长我很长我很长我很长",
            author = "作者",
            niceDate = "11小时前"
        )
    }
}