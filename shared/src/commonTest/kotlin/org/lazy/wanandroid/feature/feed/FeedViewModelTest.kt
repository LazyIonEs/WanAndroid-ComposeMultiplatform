package org.lazy.wanandroid.feature.feed

import androidx.lifecycle.ViewModelStore
import androidx.paging.LoadState
import androidx.paging.PagingDataEvent
import androidx.paging.PagingDataPresenter
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import org.lazy.wanandroid.core.network.model.ProjectCategory
import org.lazy.wanandroid.core.network.model.Article
import org.lazy.wanandroid.core.network.model.ArticleList
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModelTest {
    private val owner = ViewModelStore()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        owner.clear()
        Dispatchers.resetMain()
    }

    @Test
    fun categoriesDecodeOnceAndOnlyAValidSelectionChangesThePagerInput() = runTest {
        val service = FakePagingService().apply {
            categoryResponse = { listOf(
                ProjectCategory(294, "&lt;b&gt;Kotlin&lt;/b&gt; &amp; Compose"),
                ProjectCategory(294, "重复分类"),
                ProjectCategory(402, "跨平台"),
            ) }
        }
        val model = ownedModel(service)
        assertTrue(model.categories.value.loading)
        runCurrent()

        val categories = model.categories.value
        assertFalse(categories.loading)
        assertTrue(categories.loaded)
        assertEquals(listOf(294, 402), categories.items.map { it.id })
        assertEquals("<b>Kotlin</b> & Compose", categories.items.first().name)
        assertEquals(294, categories.selectedId)
        model.selectCategory(999)
        assertEquals(categories, model.categories.value)
        model.selectCategory(402)
        assertEquals(402, model.categories.value.selectedId)
    }

    @Test
    fun failedCategoriesCanRetryWithoutAnUnselectedProjectRequest() = runTest {
        var attempts = 0
        val service = FakePagingService().apply {
            categoryResponse = {
                if (attempts++ == 0) error("分类加载失败")
                listOf(ProjectCategory(402, "跨平台"))
            }
        }
        val model = ownedModel(service)
        runCurrent()
        assertEquals("分类加载失败", model.categories.value.error)
        assertFalse(model.categories.value.loading)
        assertNull(model.categories.value.selectedId)
        assertTrue(service.requests.isEmpty())

        model.retryCategories()
        runCurrent()
        assertNull(model.categories.value.error)
        assertEquals(402, model.categories.value.selectedId)
    }

    @Test
    fun emptyCategoriesExposeEmptyStateAndPullRefreshCanLoadThemLater() = runTest {
        val service = FakePagingService().apply { categoryResponse = { emptyList() } }
        val model = ownedModel(service)
        runCurrent()
        assertTrue(model.categories.value.loaded)
        assertFalse(model.categories.value.loading)
        assertTrue(model.categories.value.items.isEmpty())
        assertNull(model.categories.value.selectedId)

        service.categoryResponse = { listOf(ProjectCategory(294, "完整项目")) }
        model.refreshCategoriesIfNeeded()
        runCurrent()
        assertEquals(294, model.categories.value.selectedId)
    }

    @Test
    fun repeatedRefreshDoesNotStartDuplicateCategoriesRequests() = runTest {
        var attempts = 0
        val response = CompletableDeferred<List<ProjectCategory>>()
        val service = FakePagingService().apply {
            categoryResponse = {
                attempts++
                response.await()
            }
        }
        val model = ownedModel(service)
        model.retryCategories()
        model.refreshCategoriesIfNeeded()
        runCurrent()
        assertEquals(1, attempts)
        response.complete(emptyList())
        runCurrent()
        assertFalse(model.categories.value.loading)
    }

    @Test
    fun clearedOwnerIgnoresEvenAnUncancellableCategoriesResponse() = runTest {
        val response = CompletableDeferred<List<ProjectCategory>>()
        val service = FakePagingService().apply {
            categoryResponse = { withContext(NonCancellable) { response.await() } }
        }
        val model = ownedModel(service)
        runCurrent()
        val beforeClose = model.categories.value
        owner.clear()
        response.complete(listOf(ProjectCategory(294, "迟到的分类")))
        runCurrent()
        assertEquals(beforeClose, model.categories.value)
    }

    @Test
    fun categorySwitchClearsThePreviousListThroughoutSlowLoadFailureAndRetry() = runTest {
        val firstAttempt = CompletableDeferred<ArticleList>()
        val retryAttempt = CompletableDeferred<ArticleList>()
        var categoryAttempts = 0
        val service = FakePagingService().apply {
            response = { request ->
                when (request.categoryId) {
                    294 -> projectPage(294)
                    else -> if (categoryAttempts++ == 0) firstAttempt.await() else retryAttempt.await()
                }
            }
        }
        val model = ownedModel(service)
        val presenter = TestArticlePresenter()
        backgroundScope.launch { model.articles.collectLatest { presenter.collectFrom(it) } }
        runCurrent()
        assertEquals(listOf(294), presenter.articleIds)

        model.selectCategory(402)
        runCurrent()
        assertTrue(presenter.articleIds.isEmpty())
        assertIs<LoadState.Loading>(presenter.loadStateFlow.value?.refresh)

        firstAttempt.completeExceptionally(IllegalStateException("新分类暂时无法加载"))
        runCurrent()
        assertTrue(presenter.articleIds.isEmpty())
        assertIs<LoadState.Error>(presenter.loadStateFlow.value?.refresh)

        presenter.retry()
        runCurrent()
        assertTrue(presenter.articleIds.isEmpty())
        assertIs<LoadState.Loading>(presenter.loadStateFlow.value?.refresh)

        retryAttempt.complete(projectPage(402))
        runCurrent()
        assertEquals(listOf(402), presenter.articleIds)
        assertIs<LoadState.NotLoading>(presenter.loadStateFlow.value?.refresh)
        assertEquals(listOf(294, 402, 402), service.requests.map { it.categoryId })
    }

    @Test
    fun sameCategoryRefreshKeepsItsRowsWhileLoadingAndAfterFailure() = runTest {
        val refreshedPage = CompletableDeferred<ArticleList>()
        var attempts = 0
        val service = FakePagingService().apply {
            response = { if (attempts++ == 0) projectPage(294) else refreshedPage.await() }
        }
        val model = ownedModel(service)
        val presenter = TestArticlePresenter()
        backgroundScope.launch { model.articles.collectLatest { presenter.collectFrom(it) } }
        runCurrent()
        assertEquals(listOf(294), presenter.articleIds)

        presenter.refresh()
        runCurrent()
        assertEquals(listOf(294), presenter.articleIds)
        assertIs<LoadState.Loading>(presenter.loadStateFlow.value?.refresh)

        refreshedPage.completeExceptionally(IllegalStateException("刷新暂时失败"))
        runCurrent()
        assertEquals(listOf(294), presenter.articleIds)
        assertIs<LoadState.Error>(presenter.loadStateFlow.value?.refresh)
    }

    private fun ownedModel(service: FakePagingService) = FeedViewModel("projects", service).also {
        owner.put("projects", it)
    }
}

private class TestArticlePresenter : PagingDataPresenter<Article>(Dispatchers.Main) {
    val articleIds get() = snapshot().items.map { it.id }
    override suspend fun presentPagingDataEvent(event: PagingDataEvent<Article>) = Unit
}

private fun projectPage(articleId: Int) = ArticleList(
    curPage = 1,
    datas = listOf(Article(id = articleId, link = "https://example.com/$articleId", title = "项目 $articleId")),
    offset = 0, over = true, pageCount = 1, size = 20, total = 1,
)
