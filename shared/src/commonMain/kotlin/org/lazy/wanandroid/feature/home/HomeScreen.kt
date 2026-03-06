package org.lazy.wanandroid.feature.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.paging.compose.collectAsLazyPagingItems
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalAnimationApi::class)
@Composable
fun HomeScreen(
    onTopicClick: () -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val lazyPagingItems = viewModel.articleListFlow.collectAsLazyPagingItems()
        AnimatedContent(
            targetState = lazyPagingItems.loadState.refresh,
            transitionSpec = {
                if (initialState == LoadState.Loading && targetState is LoadState.NotLoading) {
                    val enterTransition = slideInVertically(
                        initialOffsetY = { fullHeight -> fullHeight / 2 },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow,
                        )
                    ) + fadeIn(animationSpec = tween())
                    val exitTransition = fadeOut(animationSpec = tween())
                    enterTransition togetherWith exitTransition
                } else {
                    fadeIn(animationSpec = tween()) togetherWith fadeOut(animationSpec = tween())
                }
            }) { loadState ->
            when (loadState) {
                is LoadState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    ContainedLoadingIndicator()
                }

                is LoadState.Error -> Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Error: ${loadState.error.message ?: "Unknown error"}")
                    Button(
                        onClick = { lazyPagingItems.retry() },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Retry")
                    }
                }

                else -> LazyColumn(
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
                        val article = lazyPagingItems[index]
                        Row(
                            Modifier.animateItem()
                        ) {
                            Text(
                                article?.title ?: "",
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                    if (lazyPagingItems.loadState.append is LoadState.Loading) {
                        item {
                            Box(Modifier.animateItem()) {
                                LoadingIndicator()
                            }
                        }
                    }
                }
            }
        }
    }
}