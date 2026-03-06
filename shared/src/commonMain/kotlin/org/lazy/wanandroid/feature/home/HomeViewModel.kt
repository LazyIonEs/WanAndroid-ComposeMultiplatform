package org.lazy.wanandroid.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import org.lazy.wanandroid.core.data.repository.HomeRepository
import org.lazy.wanandroid.core.network.model.ArticleList

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val data: ArticleList) : HomeUiState
    data class Error(val message: String?) : HomeUiState
}

class HomeViewModel(private val repository: HomeRepository) : ViewModel() {

    /**
     * A [Flow] of paging data containing the list of articles,
     * cached within the [viewModelScope] for lifecycle-aware persistence.
     */
    val articleListFlow = repository.getArticleListStream().cachedIn(viewModelScope)

}