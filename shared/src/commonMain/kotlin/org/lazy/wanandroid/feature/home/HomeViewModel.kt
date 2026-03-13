package org.lazy.wanandroid.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import org.lazy.wanandroid.core.data.repository.HomeRepository

class HomeViewModel(repository: HomeRepository) : ViewModel() {


    /**
     * A reactive stream of paginated article data, cached within the [viewModelScope]
     * to maintain the paging state across configuration changes.
     */
    val articleList = repository.getArticleListStream().cachedIn(viewModelScope)

    /**
     * A reactive stream providing the list of pinned or "top" articles.
     */
    val articleTop = repository
        .getArticleTop()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )
}