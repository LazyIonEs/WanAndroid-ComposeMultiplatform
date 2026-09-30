package org.lazy.wanandroid.feature.feed

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.lazy.wanandroid.core.data.source.ArticleService
import org.lazy.wanandroid.core.network.model.Article
import org.lazy.wanandroid.core.network.model.ArticleList
import org.lazy.wanandroid.core.network.model.ProjectCategory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ArticlePagingSourceTest {
    @Test
    fun homeAndPlazaStartAtZeroWhileProjectsKeepTheirSelectedCategoryAndStartAtOne() = runTest {
        val service = FakePagingService()
        ArticlePagingSource(service, "home").load(refresh())
        ArticlePagingSource(service, "plaza").load(refresh())
        ArticlePagingSource(service, "projects", 294).load(refresh())

        assertEquals(listOf(
            PageRequest("home", 0, null),
            PageRequest("plaza", 0, null),
            PageRequest("projects", 1, 294),
        ), service.requests)
    }

    @Test
    fun pagingLoadSizeCannotChangeTheApiPageOffset() = runTest {
        val service = FakePagingService().apply {
            response = { page(current = it.page + 1, count = 3) }
        }
        val source = ArticlePagingSource(service, "plaza")
        val first = assertIs<PagingSource.LoadResult.Page<Int, Article>>(source.load(refresh(loadSize = 60)))
        assertEquals(1, first.nextKey)
        val second = assertIs<PagingSource.LoadResult.Page<Int, Article>>(source.load(append(first.nextKey!!)))
        assertEquals(2, second.nextKey)
        assertEquals(listOf(0, 1), service.requests.map { it.page })
    }

    @Test
    fun emptyIntermediatePageStillAdvancesUsingTheServerMetadata() = runTest {
        val service = FakePagingService().apply { response = { page(1, emptyList(), count = 3) } }
        val result = assertIs<PagingSource.LoadResult.Page<Int, Article>>(
            ArticlePagingSource(service, "plaza").load(refresh()),
        )
        assertTrue(result.data.isEmpty())
        assertEquals(1, result.nextKey)
    }

    @Test
    fun lastPageStopsWhenEitherOverOrPageCountSaysItIsComplete() = runTest {
        val service = FakePagingService().apply { response = { page(1, count = 3, over = true) } }
        val source = ArticlePagingSource(service, "plaza")
        val over = assertIs<PagingSource.LoadResult.Page<Int, Article>>(source.load(refresh()))
        assertNull(over.nextKey)

        service.response = { page(3, count = 3, over = false) }
        val count = assertIs<PagingSource.LoadResult.Page<Int, Article>>(source.load(append(2)))
        assertNull(count.nextKey)
    }

    @Test
    fun retryingAnAppendUsesTheSamePageAndKeepsTheEarlierPage() = runTest {
        var attempts = 0
        val service = FakePagingService().apply {
            response = { request ->
                if (request.page == 1 && attempts++ == 0) error("临时断网")
                page(request.page + 1, listOf(article(request.page + 1)), count = 3)
            }
        }
        val source = ArticlePagingSource(service, "plaza")
        val first = assertIs<PagingSource.LoadResult.Page<Int, Article>>(source.load(refresh()))
        assertIs<PagingSource.LoadResult.Error<Int, Article>>(source.load(append(1)))
        val retried = assertIs<PagingSource.LoadResult.Page<Int, Article>>(source.load(append(1)))

        assertEquals(listOf(1), first.data.map { it.id })
        assertEquals(listOf(2), retried.data.map { it.id })
        assertEquals(listOf(0, 1, 1), service.requests.map { it.page })
        assertEquals(2, retried.nextKey)
    }

    @Test
    fun cancellationPropagatesInsteadOfBecomingARetryableFailure() = runTest {
        val service = FakePagingService().apply { response = { throw CancellationException("cancelled") } }
        assertFailsWith<CancellationException> {
            ArticlePagingSource(service, "plaza").load(refresh())
        }
        service.response = { page(1) }
        service.topResponse = { throw CancellationException("cancelled pins") }
        assertFailsWith<CancellationException> {
            ArticlePagingSource(service, "home").load(refresh())
        }
    }

    @Test
    fun invalidatedSourceCannotDeliverStaleResults() = runTest {
        val source = ArticlePagingSource(FakePagingService(), "projects", 294)
        source.invalidate()
        assertIs<PagingSource.LoadResult.Invalid<Int, Article>>(source.load(refresh()))
    }

    @Test
    fun homePinsAreMergedFirstAndOverlappingArticlesAcrossPagesAreRemoved() = runTest {
        val service = FakePagingService().apply {
            topResponse = { listOf(article(1).copy(title = "&lt;b&gt;置顶&lt;/b&gt;")) }
            response = { request ->
                if (request.page == 0) page(1, listOf(article(1), article(2)), count = 2)
                else page(2, listOf(article(1), article(2), article(3)), count = 2)
            }
        }
        val source = ArticlePagingSource(service, "home")
        val first = assertIs<PagingSource.LoadResult.Page<Int, Article>>(source.load(refresh()))
        val second = assertIs<PagingSource.LoadResult.Page<Int, Article>>(source.load(append(1)))
        assertEquals(listOf(1, 2), first.data.map { it.id })
        assertTrue(first.data.first().pinned)
        assertEquals("<b>置顶</b>", first.data.first().title)
        assertEquals(listOf(3), second.data.map { it.id })
        assertEquals(1, service.topCalls)

        // Repeated loads for the same page must not accidentally filter out their own data.
        val repeated = assertIs<PagingSource.LoadResult.Page<Int, Article>>(source.load(append(1)))
        assertEquals(second.data, repeated.data)
    }

    @Test
    fun missingArticleIdsUseLinksForDeduplication() = runTest {
        val service = FakePagingService().apply {
            response = { page(1, listOf(
                article(1).copy(id = null), article(2).copy(id = null), article(1).copy(id = null),
            )) }
        }
        val result = assertIs<PagingSource.LoadResult.Page<Int, Article>>(
            ArticlePagingSource(service, "plaza").load(refresh()),
        )
        assertEquals(listOf("https://example.com/1", "https://example.com/2"), result.data.map { it.link })
    }

    @Test
    fun pinsSurviveOptionalEndpointFailureAcrossRefreshWithoutDoubleDecoding() = runTest {
        val pins = PinnedArticleCache()
        val service = FakePagingService().apply {
            topResponse = { listOf(article(1).copy(title = "&lt;b&gt;置顶&lt;/b&gt;")) }
        }
        val original = assertIs<PagingSource.LoadResult.Page<Int, Article>>(
            ArticlePagingSource(service, "home", pins = pins).load(refresh()),
        )
        service.topResponse = { error("置顶接口暂不可用") }
        val refreshed = assertIs<PagingSource.LoadResult.Page<Int, Article>>(
            ArticlePagingSource(service, "home", pins = pins).load(refresh()),
        )
        assertEquals(original.data, refreshed.data)
        assertEquals("<b>置顶</b>", refreshed.data.first().title)
    }

    @Test
    fun refreshAlwaysReturnsToTheFirstPageToFetchNewArticles() {
        val state = PagingState<Int, Article>(
            pages = listOf(PagingSource.LoadResult.Page(listOf(article(30)), null, 4)),
            anchorPosition = 0,
            config = PagingConfig(pageSize = 20),
            leadingPlaceholderCount = 0,
        )
        assertEquals(0, ArticlePagingSource(FakePagingService(), "home").getRefreshKey(state))
        assertEquals(1, ArticlePagingSource(FakePagingService(), "projects", 402).getRefreshKey(state))
    }
}

internal data class PageRequest(val section: String, val page: Int, val categoryId: Int?)

internal class FakePagingService : ArticleService {
    val requests = mutableListOf<PageRequest>()
    var topCalls = 0
    var response: suspend (PageRequest) -> ArticleList = { page(1) }
    var topResponse: suspend () -> List<Article> = { emptyList() }
    var categoryResponse: suspend () -> List<ProjectCategory> = {
        listOf(ProjectCategory(294, "完整项目"), ProjectCategory(402, "跨平台"))
    }
    override suspend fun articles(section: String, page: Int, categoryId: Int?): ArticleList {
        val request = PageRequest(section, page, categoryId)
        requests += request
        return response(request)
    }
    override suspend fun topArticles(): List<Article> {
        topCalls++
        return topResponse()
    }
    override suspend fun categories(): List<ProjectCategory> = categoryResponse()
}

private fun refresh(loadSize: Int = 20) = PagingSource.LoadParams.Refresh<Int>(
    key = null, loadSize = loadSize, placeholdersEnabled = false,
)

private fun append(page: Int) = PagingSource.LoadParams.Append(
    key = page, loadSize = 20, placeholdersEnabled = false,
)

private fun article(id: Int) = Article(id = id, link = "https://example.com/$id", title = "文章 $id")

private fun page(
    current: Int,
    articles: List<Article> = listOf(article(1)),
    count: Int = 1,
    over: Boolean = current >= count,
) = ArticleList(
    curPage = current, datas = articles, offset = (current - 1) * 20,
    over = over, pageCount = count, size = 20, total = count * 20,
)
