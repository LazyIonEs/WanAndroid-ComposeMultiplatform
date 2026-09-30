package org.lazy.wanandroid.feature.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.lazy.wanandroid.articleFirstPage
import org.lazy.wanandroid.core.data.source.ArticleService
import org.lazy.wanandroid.core.data.source.NetworkDataSource
import org.lazy.wanandroid.core.network.model.Article
import org.lazy.wanandroid.feedErrorMessage
import org.lazy.wanandroid.toDisplayCategory

@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModel(
    private val section: String,
    private val service: ArticleService,
) : ViewModel() {
    private val mutableCategories = MutableStateFlow(CategoryState(loading = section == "projects"))
    val categories = mutableCategories.asStateFlow()
    private var categoriesJob: Job? = null
    private val pinnedArticles = PinnedArticleCache()

    val articles: Flow<PagingData<Article>> = (if (section == "projects") {
        categories.map { it.selectedId }.distinctUntilChanged().flatMapLatest { categoryId ->
            if (categoryId == null) flowOf(PagingData.empty()) else pager(categoryId).flow.onStart {
                // A new category must not display the previous category while its first page
                // loads (or fails). This runs once per category, not on same-category refresh.
                emit(PagingData.empty(sourceLoadStates = LoadStates(
                    refresh = LoadState.Loading,
                    prepend = LoadState.NotLoading(endOfPaginationReached = true),
                    append = LoadState.NotLoading(endOfPaginationReached = false),
                )))
            }
        }
    } else {
        pager(categoryId = null).flow
    }).cachedIn(viewModelScope)

    init {
        articleFirstPage(section) // Validate before a collector starts a network request.
        if (section == "projects") loadCategories()
    }

    fun selectCategory(id: Int) {
        val current = categories.value
        if (current.selectedId != id && current.items.any { it.id == id }) {
            mutableCategories.value = current.copy(selectedId = id)
        }
    }

    fun retryCategories() = loadCategories()

    fun refreshCategoriesIfNeeded() {
        if (categories.value.items.isEmpty()) loadCategories()
    }

    private fun pager(categoryId: Int?): Pager<Int, Article> = Pager(
        config = PagingConfig(
            pageSize = NetworkDataSource.NETWORK_PAGE_SIZE,
            initialLoadSize = NetworkDataSource.NETWORK_PAGE_SIZE,
            prefetchDistance = 5,
            enablePlaceholders = false,
        ),
        pagingSourceFactory = { ArticlePagingSource(service, section, categoryId, pinnedArticles) },
    )

    private fun loadCategories() {
        if (section != "projects" || categoriesJob?.isActive == true) return
        mutableCategories.value = categories.value.copy(loading = true, error = null)
        categoriesJob = viewModelScope.launch {
            try {
                val items = service.categories().map { it.toDisplayCategory() }.distinctBy { it.id }
                ensureActive()
                val selected = categories.value.selectedId?.takeIf { id -> items.any { it.id == id } }
                    ?: items.firstOrNull()?.id
                mutableCategories.value = CategoryState(items = items, selectedId = selected, loaded = true)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                ensureActive()
                mutableCategories.value = categories.value.copy(
                    loading = false,
                    error = error.feedErrorMessage(),
                    loaded = true,
                )
            }
        }
    }
}
