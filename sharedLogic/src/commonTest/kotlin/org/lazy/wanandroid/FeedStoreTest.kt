package org.lazy.wanandroid

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.lazy.wanandroid.core.data.source.ArticleService
import org.lazy.wanandroid.core.network.model.Article
import org.lazy.wanandroid.core.network.model.ArticleList
import org.lazy.wanandroid.core.network.model.ProjectCategory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FeedStoreTest {
    @Test
    fun successfulEmptyPagesExposeProgressAndFailedAppendPreservesTheCursor() = runTest {
        var fail = true
        val service = FakeArticleService().apply {
            articleResponse = { request ->
                if (request.page == 2 && fail) error("网络暂时不可用")
                page(request.page + 1, emptyList(), pageCount = 3)
            }
        }
        val store = FeedStore(service, this)
        assertNull(store.state.value.plaza.loadedPage)
        store.refresh("plaza")
        runCurrent()
        assertEquals(0, store.state.value.plaza.loadedPage)
        store.loadMore("plaza")
        runCurrent()
        assertEquals(1, store.state.value.plaza.loadedPage)
        assertTrue(store.state.value.plaza.hasMore)
        store.loadMore("plaza")
        runCurrent()
        assertEquals(1, store.state.value.plaza.loadedPage)
        assertTrue(store.state.value.plaza.failedToLoadMore)
        fail = false
        store.loadMore("plaza")
        runCurrent()
        assertEquals(2, store.state.value.plaza.loadedPage)
        assertFalse(store.state.value.plaza.hasMore)
        assertEquals(listOf(0, 1, 2, 2), service.requests.map { it.page })
    }

    @Test
    fun plazaStartsAtZeroAndProjectsStartAtOneWithSelectedCategory() = runTest {
        val service = FakeArticleService()
        val store = FeedStore(service, this)

        store.refresh("plaza")
        store.refresh("projects")
        runCurrent()

        assertEquals(Request("plaza", 0, null), service.requests[0])
        assertEquals(Request("projects", 1, 294), service.requests[1])
        assertEquals(294, store.state.value.selectedProjectId)
        assertTrue(store.state.value.plaza.loaded)
        assertTrue(store.state.value.projects.loaded)
    }

    @Test
    fun appendFailureKeepsArticlesAndRetriesTheSamePageBeforeAdvancing() = runTest {
        var secondPageAttempts = 0
        val service = FakeArticleService().apply {
            articleResponse = { request ->
                when (request.page) {
                    0 -> page(1, listOf(article(1)), pageCount = 3)
                    1 -> {
                        if (secondPageAttempts++ == 0) error("网络暂时不可用")
                        page(2, listOf(article(1), article(2)), pageCount = 3)
                    }
                    else -> page(3, listOf(article(3)), pageCount = 3, over = true)
                }
            }
        }
        val store = FeedStore(service, this)
        store.refresh("plaza")
        runCurrent()

        store.loadMore("plaza")
        store.loadMore("plaza") // Repeated UI events must not start two requests.
        runCurrent()
        assertEquals(listOf(1), store.state.value.plaza.articles.map { it.id })
        assertEquals("网络暂时不可用", store.state.value.plaza.error)
        assertFalse(store.state.value.plaza.loadingMore)
        assertTrue(store.state.value.plaza.failedToLoadMore)

        store.loadMore("plaza")
        runCurrent()
        assertEquals(listOf(1, 2), store.state.value.plaza.articles.map { it.id })
        assertNull(store.state.value.plaza.error)
        assertFalse(store.state.value.plaza.failedToLoadMore)

        store.loadMore("plaza")
        runCurrent()
        store.loadMore("plaza")
        runCurrent()
        assertEquals(listOf(0, 1, 1, 2), service.requests.map { it.page })
        assertEquals(listOf(1, 2, 3), store.state.value.plaza.articles.map { it.id })
        assertFalse(store.state.value.plaza.hasMore)
    }

    @Test
    fun refreshFailurePreservesVisibleArticlesAndCanBeRetried() = runTest {
        var fail = false
        val service = FakeArticleService().apply {
            articleResponse = {
                if (fail) error("刷新失败")
                page(1, listOf(article(7)))
            }
        }
        val store = FeedStore(service, this)
        store.refresh("plaza")
        runCurrent()

        fail = true
        store.refresh("plaza")
        assertTrue(store.state.value.plaza.refreshing)
        assertEquals(listOf(7), store.state.value.plaza.articles.map { it.id })
        runCurrent()
        assertTrue(store.state.value.plaza.loaded)
        assertFalse(store.state.value.plaza.refreshing)
        assertEquals("刷新失败", store.state.value.plaza.error)
        assertEquals(listOf(7), store.state.value.plaza.articles.map { it.id })

        fail = false
        store.refresh("plaza")
        runCurrent()
        assertNull(store.state.value.plaza.error)
        assertEquals(listOf(0, 0, 0), service.requests.map { it.page })
    }

    @Test
    fun previousCategoryCannotOverwriteNewSelectionEvenWhenItIgnoresCancellation() = runTest {
        val oldResponse = CompletableDeferred<ArticleList>()
        val newResponse = CompletableDeferred<ArticleList>()
        val service = FakeArticleService().apply {
            articleResponse = { request ->
                // Model a callback/native client which delivers a response after cancellation.
                withContext(NonCancellable) {
                    if (request.categoryId == 294) oldResponse.await() else newResponse.await()
                }
            }
        }
        val store = FeedStore(service, this)
        store.refresh("projects")
        runCurrent()
        store.selectProject(402)
        runCurrent()

        newResponse.complete(page(1, listOf(article(402))))
        runCurrent()
        assertEquals(402, store.state.value.selectedProjectId)
        assertEquals(listOf(402), store.state.value.projects.articles.map { it.id })

        oldResponse.complete(page(1, listOf(article(294))))
        runCurrent()
        assertEquals(402, store.state.value.selectedProjectId)
        assertEquals(listOf(402), store.state.value.projects.articles.map { it.id })
        assertNull(store.state.value.projects.error)
        assertFalse(store.state.value.projects.loading)
        assertEquals(listOf(1, 1), service.requests.map { it.page })
    }

    @Test
    fun categoriesFailureIsRetryableAndDoesNotRequestAnUnselectedProject() = runTest {
        var attempts = 0
        val service = FakeArticleService().apply {
            categoryResponse = {
                if (attempts++ == 0) error("分类加载失败")
                listOf(ProjectCategory(294, "完整项目"))
            }
        }
        val store = FeedStore(service, this)
        store.refresh("projects")
        runCurrent()
        assertEquals("分类加载失败", store.state.value.categoriesError)
        assertFalse(store.state.value.categoriesLoading)
        assertTrue(service.requests.isEmpty())

        store.refresh("projects")
        runCurrent()
        assertNull(store.state.value.categoriesError)
        assertEquals(listOf(Request("projects", 1, 294)), service.requests)
        assertTrue(store.state.value.projects.loaded)
    }

    @Test
    fun emptyCategoriesFinishWithAnEmptyState() = runTest {
        val service = FakeArticleService().apply { categoryResponse = { emptyList() } }
        val store = FeedStore(service, this)
        store.refresh("projects")
        runCurrent()
        store.loadMore("projects")
        store.selectProject(999)
        runCurrent()

        assertTrue(store.state.value.projects.loaded)
        assertFalse(store.state.value.projects.hasMore)
        assertFalse(store.state.value.categoriesLoading)
        assertNull(store.state.value.selectedProjectId)
        assertTrue(service.requests.isEmpty())
    }

    @Test
    fun articleAndCategoryHtmlAreDecodedAndMissingIdsUseLinksForDeduplication() = runTest {
        val service = FakeArticleService().apply {
            categoryResponse = { listOf(ProjectCategory(294, "<b>Kotlin</b> &amp; Compose")) }
            articleResponse = {
                page(1, listOf(
                    Article(link = "https://example.com/one", title = "<b>Compose</b> &mdash; UI", desc = "<p>Android &amp; iOS</p>"),
                    Article(link = "https://example.com/two", title = "第二篇"),
                    Article(link = "https://example.com/one", title = "重复文章"),
                ))
            }
        }
        val store = FeedStore(service, this)
        store.refresh("projects")
        runCurrent()

        assertEquals("Kotlin & Compose", store.state.value.categories.single().name)
        val articles = store.state.value.projects.articles
        assertEquals(2, articles.size)
        assertEquals("Compose — UI", articles[0].title)
        assertEquals("Android & iOS", articles[0].desc)
        assertNull(articles[1].desc)
        assertNull(articles[1].author)
    }

    @Test
    fun homePinsArticlesOnceAndKeepsThemWhenTopEndpointFailsOnRefresh() = runTest {
        var topFails = false
        val service = FakeArticleService().apply {
            articleResponse = { page(1, listOf(article(1), article(2))) }
            topResponse = {
                if (topFails) error("置顶接口不可用")
                listOf(article(1).copy(title = "&lt;b&gt;置顶&lt;/b&gt;"))
            }
        }
        val store = FeedStore(service, this)
        store.refresh("home")
        runCurrent()
        assertEquals(listOf(1, 2), store.state.value.home.articles.map { it.id })
        assertTrue(store.state.value.home.articles.first().pinned)

        topFails = true
        store.refresh("home")
        runCurrent()
        assertEquals(listOf(1, 2), store.state.value.home.articles.map { it.id })
        assertTrue(store.state.value.home.articles.first().pinned)
        assertEquals("<b>置顶</b>", store.state.value.home.articles.first().title)
        assertNull(store.state.value.home.error)
    }

    @Test
    fun appendingDoesNotDecodeLiteralHtmlInExistingArticlesAgain() = runTest {
        val service = FakeArticleService().apply {
            articleResponse = { request ->
                if (request.page == 0) {
                    page(1, listOf(article(1).copy(
                        title = "&lt;b&gt;Compose&lt;/b&gt; &amp; UI",
                        desc = "保留 &lt;span&gt; 标签示例",
                    )), pageCount = 2)
                } else {
                    page(2, listOf(article(2)), pageCount = 2)
                }
            }
        }
        val store = FeedStore(service, this)
        store.refresh("plaza")
        runCurrent()
        val original = store.state.value.plaza.articles.first()
        assertEquals("<b>Compose</b> & UI", original.title)

        store.loadMore("plaza")
        runCurrent()
        assertEquals(original, store.state.value.plaza.articles.first())
    }

    @Test
    fun cancellingOwnerPreventsLateCategoriesAndFurtherActionsFromChangingState() = runTest {
        val response = CompletableDeferred<List<ProjectCategory>>()
        val service = FakeArticleService().apply {
            categoryResponse = { withContext(NonCancellable) { response.await() } }
        }
        val owner = CoroutineScope(coroutineContext + Job(coroutineContext[Job]))
        val store = FeedStore(service, owner)
        store.refresh("projects")
        runCurrent()
        val stateBeforeClose = store.state.value

        owner.cancel()
        response.complete(listOf(ProjectCategory(294, "迟到的分类")))
        runCurrent()
        store.refresh("plaza")
        store.selectProject(294)
        store.loadMore("projects")
        runCurrent()

        assertEquals(stateBeforeClose, store.state.value)
        assertTrue(service.requests.isEmpty())
    }

    @Test
    fun cancellingOwnerDoesNotPublishAnErrorFromAnUncancellableRequest() = runTest {
        val response = CompletableDeferred<Unit>()
        val service = FakeArticleService().apply {
            articleResponse = {
                withContext(NonCancellable) { response.await() }
                error("关闭后的网络异常")
            }
        }
        val owner = CoroutineScope(coroutineContext + Job(coroutineContext[Job]))
        val store = FeedStore(service, owner)
        store.refresh("plaza")
        runCurrent()
        val stateBeforeClose = store.state.value

        owner.cancel()
        response.complete(Unit)
        runCurrent()
        assertEquals(stateBeforeClose, store.state.value)
        assertNull(store.state.value.plaza.error)
    }
}

private data class Request(val section: String, val page: Int, val categoryId: Int?)

private class FakeArticleService : ArticleService {
    val requests = mutableListOf<Request>()
    var articleResponse: suspend (Request) -> ArticleList = { page(1, listOf(article(1))) }
    var categoryResponse: suspend () -> List<ProjectCategory> = {
        listOf(ProjectCategory(294, "完整项目"), ProjectCategory(402, "跨平台"))
    }
    var topResponse: suspend () -> List<Article> = { emptyList() }

    override suspend fun articles(section: String, page: Int, categoryId: Int?): ArticleList {
        val request = Request(section, page, categoryId)
        requests += request
        return articleResponse(request)
    }

    override suspend fun topArticles() = topResponse()
    override suspend fun categories() = categoryResponse()
}

private fun article(id: Int) = Article(id = id, link = "https://example.com/$id", title = "文章 $id")

private fun page(
    currentPage: Int,
    articles: List<Article>,
    pageCount: Int = 1,
    over: Boolean = currentPage >= pageCount,
) = ArticleList(
    curPage = currentPage,
    datas = articles,
    offset = (currentPage - 1) * 20,
    over = over,
    pageCount = pageCount,
    size = 20,
    total = pageCount * 20,
)
