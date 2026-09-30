package org.lazy.wanandroid.feature.feed

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import org.lazy.wanandroid.articleFirstPage
import org.lazy.wanandroid.core.data.source.ArticleService
import org.lazy.wanandroid.core.network.model.Article
import org.lazy.wanandroid.feedKey
import org.lazy.wanandroid.hasMorePages
import org.lazy.wanandroid.toDisplayArticle

/** Keeps already decoded pins across refresh generations when the optional top API fails. */
internal class PinnedArticleCache {
    var articles: List<Article> = emptyList()
}

internal class ArticlePagingSource(
    private val service: ArticleService,
    private val section: String,
    private val categoryId: Int? = null,
    private val pins: PinnedArticleCache = PinnedArticleCache(),
) : PagingSource<Int, Article>() {
    private val firstPage = articleFirstPage(section)
    private val pageKeys = mutableMapOf<Int, Set<String>>()

    init {
        require(section != "projects" || categoryId != null) { "Projects require a category" }
    }

    // A refresh of these chronological feeds must fetch new articles and pins from page one.
    // This source only appends; pages are retained until the next refresh generation.
    override fun getRefreshKey(state: PagingState<Int, Article>): Int = firstPage

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Article> {
        val page = params.key ?: firstPage
        return try {
            // The API uses fixed page offsets. Never substitute Paging's requested loadSize
            // for page_size, otherwise the initial request can skip articles on append.
            val response = service.articles(section, page, categoryId)
            currentCoroutineContext().ensureActive()
            val pinned = if (section == "home" && page == firstPage) {
                try {
                    service.topArticles().map { it.toDisplayArticle().copy(pinned = true) }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    pins.articles
                }
            } else emptyList()
            currentCoroutineContext().ensureActive()
            if (invalid) return LoadResult.Invalid()

            if (params is LoadParams.Refresh) pageKeys.clear()
            // Keep retrying the same page idempotent, and suppress API overlap across pages.
            val previousKeys = pageKeys.filterKeys { it != page }.values.flatten().toSet()
            val articles = (pinned + response.datas.map { it.toDisplayArticle() })
                .distinctBy { it.feedKey }
                .filterNot { it.feedKey in previousKeys }
            pageKeys[page] = articles.map { it.feedKey }.toSet()
            if (section == "home" && page == firstPage) pins.articles = pinned

            LoadResult.Page(
                data = articles,
                prevKey = null,
                // An empty page can still have a next page; trust the API's paging metadata.
                nextKey = if (response.hasMorePages) page + 1 else null,
            )
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            currentCoroutineContext().ensureActive()
            LoadResult.Error(error)
        }
    }
}
