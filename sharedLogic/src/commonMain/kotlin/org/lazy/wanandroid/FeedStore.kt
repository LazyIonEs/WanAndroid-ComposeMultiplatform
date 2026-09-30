package org.lazy.wanandroid

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.lazy.wanandroid.core.data.source.ArticleService

/** All mutations run on the UI dispatcher. UI layers only observe state and dispatch actions. */
class FeedStore(
    private val service: ArticleService,
    private val scope: CoroutineScope,
) {
    private val mutableState = MutableStateFlow(WanAndroidState())
    val state = mutableState.asStateFlow()
    private val jobs = mutableMapOf<String, Job>()
    private val generations = mutableMapOf<String, Int>()
    private val nextPages = mutableMapOf<String, Int>()
    private var categoriesJob: Job? = null

    fun refresh(section: String) {
        require(section in listOf("home", "plaza", "projects"))
        if (!scope.isActive) return
        if (section == "projects" && state.value.categories.isEmpty()) {
            loadCategories()
        } else {
            load(section, append = false)
        }
    }

    fun loadMore(section: String) {
        if (!scope.isActive) return
        val feed = state.value.feed(section)
        if (!feed.loaded || !feed.hasMore || feed.loading || feed.refreshing || feed.loadingMore) return
        load(section, append = true)
    }

    fun selectProject(categoryId: Int) {
        if (!scope.isActive) return
        if (state.value.categories.none { it.id == categoryId }) return
        if (state.value.selectedProjectId == categoryId && state.value.projects.loaded) return
        jobs.remove("projects")?.cancel()
        mutableState.value = state.value.copy(selectedProjectId = categoryId, projects = FeedState())
        load("projects", append = false)
    }

    private fun loadCategories() {
        if (categoriesJob?.isActive == true) return
        mutableState.value = state.value.copy(categoriesLoading = true, categoriesError = null)
        categoriesJob = scope.launch {
            try {
                val categories = service.categories().map { it.toDisplayCategory() }
                ensureActive()
                mutableState.value = state.value.copy(
                    categories = categories,
                    selectedProjectId = categories.firstOrNull()?.id,
                    categoriesLoading = false,
                    projects = if (categories.isEmpty()) {
                        FeedState(loaded = true, hasMore = false)
                    } else {
                        state.value.projects
                    },
                )
                if (categories.isNotEmpty()) load("projects", append = false)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                ensureActive()
                mutableState.value = state.value.copy(
                    categoriesLoading = false,
                    categoriesError = error.feedErrorMessage(),
                )
            }
        }
    }

    private fun load(section: String, append: Boolean) {
        val category = state.value.selectedProjectId
        if (section == "projects" && category == null) return
        jobs.remove(section)?.cancel()
        val generation = (generations[section] ?: 0) + 1
        generations[section] = generation
        val before = state.value.feed(section)
        val page = if (append) {
            nextPages[section] ?: articleFirstPage(section)
        } else {
            articleFirstPage(section)
        }
        update(
            section,
            before.copy(
                loading = !append && before.articles.isEmpty(),
                refreshing = !append && before.articles.isNotEmpty(),
                loadingMore = append,
                error = null,
                failedToLoadMore = false,
            ),
        )
        jobs[section] = scope.launch {
            try {
                val response = service.articles(section, page, category)
                ensureActive()
                val pinned = if (section == "home" && !append) {
                    try {
                        service.topArticles().map { it.toDisplayArticle().copy(pinned = true) }
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (_: Exception) {
                        before.articles.filter { it.pinned }
                    }
                } else {
                    emptyList()
                }
                ensureActive()
                if (generations[section] != generation) return@launch
                // Existing articles are already decoded. Decoding them again can turn escaped
                // literal HTML (e.g. &lt;b&gt;) into markup and remove it on the next page.
                val articles = ((if (append) before.articles else pinned) + response.datas.map { it.toDisplayArticle() })
                    .distinctBy { it.feedKey }
                nextPages[section] = page + 1
                update(
                    section,
                    FeedState(
                        articles = articles,
                        loaded = true,
                        hasMore = response.hasMorePages,
                        loadedPage = page,
                    ),
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                ensureActive()
                if (generations[section] == generation) {
                    update(
                        section,
                        before.copy(
                            loading = false,
                            refreshing = false,
                            loadingMore = false,
                            error = error.feedErrorMessage(),
                            failedToLoadMore = append,
                        ),
                    )
                }
            }
        }
    }

    private fun update(section: String, feed: FeedState) {
        mutableState.value = when (section) {
            "home" -> state.value.copy(home = feed)
            "plaza" -> state.value.copy(plaza = feed)
            "projects" -> state.value.copy(projects = feed)
            else -> error("Unknown section")
        }
    }
}
