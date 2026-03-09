package org.lazy.wanandroid.feature.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import org.koin.compose.viewmodel.koinViewModel
import org.lazy.wanandroid.common.defaultTransition
import org.lazy.wanandroid.common.listEnterTransition
import org.lazy.wanandroid.core.network.model.Article

@Composable
fun HomeScreen(
    onTopicClick: () -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val lazyPagingItems = viewModel.articleListFlow.collectAsLazyPagingItems()
    val refreshState = lazyPagingItems.loadState.refresh
    HomeScreen(
        lazyPagingItems = lazyPagingItems,
        refreshState = refreshState,
        onTopicClick = onTopicClick,
        retry = { lazyPagingItems.retry() }
    )
}

@Composable
internal fun HomeScreen(
    lazyPagingItems: LazyPagingItems<Article>,
    refreshState: LoadState,
    onTopicClick: () -> Unit,
    retry: () -> Unit
) {
    AnimatedContent(
        modifier = Modifier.fillMaxSize(),
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
            else -> HomeScreenContent(lazyPagingItems)
        }
    }
}

@Composable
private fun HomeScreenContent(lazyPagingItems: LazyPagingItems<Article>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        items(
            count = lazyPagingItems.itemCount,
            key = { index ->
                val article = lazyPagingItems.peek(index)
                article?.id ?: index
            }
        ) { index ->
            val article = lazyPagingItems[index] ?: return@items
            HomeScreenItem(
                modifier = Modifier.animateItem(),
                article = article
            )
        }

        if (lazyPagingItems.loadState.append is LoadState.Loading) {
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
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
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
            onClick = retry,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("重试")
        }
    }
}

@Composable
private fun HomeScreenItem(modifier: Modifier, article: Article) {
    Row(modifier) {
        Text(
            article.title,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalAnimationApi::class)
@Composable
private fun HomeScreenAppendLoading(modifier: Modifier) {
    Box(
        modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        LoadingIndicator()
    }
}