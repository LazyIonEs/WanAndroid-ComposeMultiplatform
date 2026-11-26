package org.lazy.wanandroid

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import org.lazy.wanandroid.core.data.source.ArticleService
import org.lazy.wanandroid.core.network.model.Article
import org.lazy.wanandroid.core.network.model.AuthExpiredException
import org.lazy.wanandroid.core.network.model.UserInfo

/** All mutations run on the UI dispatcher. UI layers only observe state and dispatch actions. */
class FeedStore(
    private val service: ArticleService,
    private val scope: CoroutineScope,
    private val storage: SessionStorage = InMemorySessionStorage(),
) {
    private val mutableState = MutableStateFlow(
        WanAndroidState(searchHistory = readSearchHistory())
    )
    val state = mutableState.asStateFlow()
    private val jobs = mutableMapOf<String, Job>()
    private val generations = mutableMapOf<String, Int>()
    private val nextPages = mutableMapOf<String, Int>()
    private var categoriesJob: Job? = null
    private var hotKeysJob: Job? = null
    private var authJob: Job? = null
    private var coinRankJob: Job? = null
    private var coinHistoryJob: Job? = null
    private var coinRankNextPage = 1
    private var coinHistoryNextPage = 1

    companion object {
        private const val SEARCH_HISTORY_KEY = "wanandroid_search_history_v1"
        private const val MAX_SEARCH_HISTORY = 12
    }

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

    fun loadHotKeys() {
        if (!scope.isActive || hotKeysJob?.isActive == true) return
        hotKeysJob = scope.launch {
            try {
                val items = service.hotKeys().map { it.toDisplayHotKey() }
                ensureActive()
                mutableState.value = state.value.copy(hotKeys = items)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                // Keep existing hot keys on transient failure.
            }
        }
    }

    fun search(keyword: String) {
        if (!scope.isActive) return
        val trimmed = keyword.trim()
        if (trimmed.isEmpty()) {
            clearSearch()
            return
        }
        saveSearchKeyword(trimmed)
        mutableState.value = state.value.copy(searchQuery = trimmed, search = FeedState())
        loadSearchPage(trimmed, append = false)
    }

    fun loadMoreSearch() {
        if (!scope.isActive) return
        val query = state.value.searchQuery.trim()
        val feed = state.value.search
        if (query.isEmpty() || !feed.loaded || !feed.hasMore || feed.loading || feed.refreshing || feed.loadingMore) return
        loadSearchPage(query, append = true)
    }

    fun clearSearch() {
        jobs.remove("search")?.cancel()
        generations["search"] = (generations["search"] ?: 0) + 1
        mutableState.value = state.value.copy(searchQuery = "", search = FeedState(hasMore = false))
    }

    fun removeSearchHistory(keyword: String) {
        val updated = state.value.searchHistory.filterNot { it == keyword }
        writeSearchHistory(updated)
        mutableState.value = state.value.copy(searchHistory = updated)
    }

    fun clearSearchHistory() {
        writeSearchHistory(emptyList())
        mutableState.value = state.value.copy(searchHistory = emptyList())
    }

    fun restoreSession() {
        if (!scope.isActive) return
        scope.launch {
            try {
                if (!service.hasSavedSession()) return@launch
                val profile = service.userInfo()
                ensureActive()
                applyUserProfile(profile.userInfo, profile.coinInfo)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: AuthExpiredException) {
                handleAuthExpired(notify = false)
            } catch (_: Exception) {
                // Transient network failures keep local state intact until next action.
            }
        }
    }

    fun login(username: String, password: String) {
        if (!scope.isActive) return
        val trimmedUser = username.trim()
        if (trimmedUser.isEmpty() || password.isEmpty()) {
            mutableState.value = state.value.copy(authError = "请输入用户名和密码")
            return
        }
        authJob?.cancel()
        mutableState.value = state.value.copy(authLoading = true, authError = null)
        authJob = scope.launch {
            try {
                val user = service.login(trimmedUser, password)
                ensureActive()
                applyLoggedInUser(user, "欢迎回来，${user.nickname.ifBlank { user.username }}")
                fetchUserInfoInBackground()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                ensureActive()
                mutableState.value = state.value.copy(
                    authLoading = false,
                    authError = error.feedErrorMessage(),
                )
            }
        }
    }

    fun register(username: String, password: String, repassword: String) {
        if (!scope.isActive) return
        val trimmedUser = username.trim()
        if (trimmedUser.isEmpty() || password.isEmpty()) {
            mutableState.value = state.value.copy(authError = "请输入用户名和密码")
            return
        }
        if (password != repassword) {
            mutableState.value = state.value.copy(authError = "两次输入的密码不一致")
            return
        }
        authJob?.cancel()
        mutableState.value = state.value.copy(authLoading = true, authError = null)
        authJob = scope.launch {
            try {
                val user = service.register(trimmedUser, password, repassword)
                ensureActive()
                applyLoggedInUser(user, "注册成功，欢迎加入 ${user.nickname.ifBlank { user.username }}")
                fetchUserInfoInBackground()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                ensureActive()
                mutableState.value = state.value.copy(
                    authLoading = false,
                    authError = error.feedErrorMessage(),
                )
            }
        }
    }

    fun logout() {
        if (!scope.isActive) return
        authJob?.cancel()
        scope.launch {
            try {
                service.logout()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                // Local cookies are still cleared by NetworkDataSource.logout() finally block.
            } finally {
                if (scope.isActive) {
                    clearSessionState("已退出登录")
                }
            }
        }
    }

    fun refreshUserInfo() {
        if (!scope.isActive) return
        scope.launch {
            fetchUserInfoInBackground()
        }
    }

    fun clearAuthError() {
        mutableState.value = state.value.copy(authError = null)
    }

    fun clearActionMessage() {
        if (state.value.actionMessage != null) {
            mutableState.value = state.value.copy(actionMessage = null)
        }
    }

    fun toggleCollect(articleId: Int) {
        if (!scope.isActive) return
        if (state.value.user == null) {
            mutableState.value = state.value.copy(actionMessage = "请先登录后再收藏")
            return
        }
        val wasCollected = articleId in state.value.collectedIds
        val optimisticIds = if (wasCollected) {
            state.value.collectedIds.filterNot { it == articleId }
        } else {
            (state.value.collectedIds + articleId).distinct()
        }
        updateCollectedState(optimisticIds, articleId, !wasCollected)
        scope.launch {
            try {
                if (wasCollected) {
                    service.uncollectOriginId(articleId)
                } else {
                    service.collectArticle(articleId)
                }
                ensureActive()
                mutableState.value = state.value.copy(
                    actionMessage = if (wasCollected) "已取消收藏" else "已收藏",
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: AuthExpiredException) {
                handleAuthExpired(notify = true, message = error.feedErrorMessage())
            } catch (error: Exception) {
                ensureActive()
                // Roll back optimistic state on failure.
                val rolledBack = if (wasCollected) {
                    (state.value.collectedIds + articleId).distinct()
                } else {
                    state.value.collectedIds.filterNot { it == articleId }
                }
                updateCollectedState(rolledBack, articleId, wasCollected)
                mutableState.value = state.value.copy(actionMessage = error.feedErrorMessage())
            }
        }
    }

    fun refreshCollections() {
        if (!scope.isActive) return
        if (state.value.user == null) {
            mutableState.value = state.value.copy(
                collections = FeedState(loaded = true, hasMore = false, error = "请先登录后查看收藏"),
            )
            return
        }
        loadCollectionsPage(append = false)
    }

    fun loadMoreCollections() {
        if (!scope.isActive) return
        val feed = state.value.collections
        if (!feed.loaded || !feed.hasMore || feed.loading || feed.refreshing || feed.loadingMore) return
        loadCollectionsPage(append = true)
    }

    fun uncollectFromMine(id: Int, originId: Int) {
        if (!scope.isActive) return
        scope.launch {
            try {
                service.uncollectFromMine(id, originId)
                ensureActive()
                val targetArticleId = if (originId > 0) originId else id
                val updatedIds = state.value.collectedIds.filterNot { it == targetArticleId }
                val currentCollections = state.value.collections
                val updatedCollections = currentCollections.copy(
                    articles = currentCollections.articles.filterNot { it.id == id },
                )
                mutableState.value = state.value.copy(
                    collections = updatedCollections,
                    actionMessage = "已取消收藏",
                )
                updateCollectedState(updatedIds, targetArticleId, false)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: AuthExpiredException) {
                handleAuthExpired(notify = true, message = error.feedErrorMessage())
            } catch (error: Exception) {
                ensureActive()
                mutableState.value = state.value.copy(actionMessage = error.feedErrorMessage())
            }
        }
    }

    fun collectExternal(title: String, author: String, link: String) {
        if (!scope.isActive) return
        val cleanTitle = title.trim()
        val cleanAuthor = author.trim()
        val cleanLink = link.trim()
        if (cleanTitle.isEmpty() || cleanLink.isEmpty()) {
            mutableState.value = state.value.copy(actionMessage = "请填写标题和链接")
            return
        }
        scope.launch {
            try {
                val created = service.collectExternalArticle(cleanTitle, cleanAuthor, cleanLink)
                    .toDisplayCollectedArticle()
                ensureActive()
                val current = state.value.collections
                val updatedArticles = (listOf(created) + current.articles).distinctBy { it.feedKey }
                mutableState.value = state.value.copy(
                    collections = current.copy(articles = updatedArticles, loaded = true),
                    actionMessage = "已添加到收藏",
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: AuthExpiredException) {
                handleAuthExpired(notify = true, message = error.feedErrorMessage())
            } catch (error: Exception) {
                ensureActive()
                mutableState.value = state.value.copy(actionMessage = error.feedErrorMessage())
            }
        }
    }

    fun refreshCoinRank() {
        if (!scope.isActive) return
        loadCoinRankPage(append = false)
    }

    fun loadMoreCoinRank() {
        if (!scope.isActive) return
        val rank = state.value.coinRank
        if (!rank.loaded || !rank.hasMore || rank.loading || rank.refreshing || rank.loadingMore) return
        loadCoinRankPage(append = true)
    }

    fun refreshCoinHistory() {
        if (!scope.isActive) return
        if (state.value.user == null) {
            mutableState.value = state.value.copy(
                coinHistory = CoinHistoryState(loaded = true, hasMore = false, error = "请先登录后查看积分明细"),
            )
            return
        }
        loadCoinHistoryPage(append = false)
    }

    fun loadMoreCoinHistory() {
        if (!scope.isActive) return
        val history = state.value.coinHistory
        if (!history.loaded || !history.hasMore || history.loading || history.refreshing || history.loadingMore) return
        loadCoinHistoryPage(append = true)
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
                val banners = if (section == "home" && !append) {
                    try {
                        service.banners().map { it.toDisplayBanner() }
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (_: Exception) {
                        state.value.banners
                    }
                } else {
                    state.value.banners
                }
                ensureActive()
                if (generations[section] != generation) return@launch
                // Existing articles are already decoded. Decoding them again can turn escaped
                // literal HTML (e.g. &lt;b&gt;) into markup and remove it on the next page.
                val articles = ((if (append) before.articles else pinned) + response.datas.map { it.toDisplayArticle() })
                    .distinctBy { it.feedKey }
                nextPages[section] = page + 1
                if (section == "home" && !append) {
                    mutableState.value = state.value.copy(banners = banners)
                }
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

    private fun loadSearchPage(keyword: String, append: Boolean) {
        jobs.remove("search")?.cancel()
        val generation = (generations["search"] ?: 0) + 1
        generations["search"] = generation
        val before = state.value.search
        val page = if (append) nextPages["search"] ?: 0 else 0
        mutableState.value = state.value.copy(
            search = before.copy(
                loading = !append && before.articles.isEmpty(),
                refreshing = !append && before.articles.isNotEmpty(),
                loadingMore = append,
                error = null,
                failedToLoadMore = false,
            ),
        )
        jobs["search"] = scope.launch {
            try {
                val response = service.searchArticles(page, keyword)
                ensureActive()
                if (generations["search"] != generation) return@launch
                val incoming = response.datas.map { it.toDisplayArticle() }
                val articles = ((if (append) before.articles else emptyList()) + incoming).distinctBy { it.feedKey }
                nextPages["search"] = page + 1
                mutableState.value = state.value.copy(
                    search = FeedState(
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
                if (generations["search"] == generation) {
                    mutableState.value = state.value.copy(
                        search = before.copy(
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

    private fun loadCollectionsPage(append: Boolean) {
        jobs.remove("collect")?.cancel()
        val generation = (generations["collect"] ?: 0) + 1
        generations["collect"] = generation
        val before = state.value.collections
        val page = if (append) nextPages["collect"] ?: 0 else 0
        mutableState.value = state.value.copy(
            collections = before.copy(
                loading = !append && before.articles.isEmpty(),
                refreshing = !append && before.articles.isNotEmpty(),
                loadingMore = append,
                error = null,
                failedToLoadMore = false,
            ),
        )
        jobs["collect"] = scope.launch {
            try {
                val response = service.collectList(page)
                ensureActive()
                if (generations["collect"] != generation) return@launch
                val incoming = response.datas.map { it.toDisplayCollectedArticle() }
                val articles = ((if (append) before.articles else emptyList()) + incoming).distinctBy { it.feedKey }
                nextPages["collect"] = page + 1
                mutableState.value = state.value.copy(
                    collections = FeedState(
                        articles = articles,
                        loaded = true,
                        hasMore = response.hasMorePages,
                        loadedPage = page,
                    ),
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: AuthExpiredException) {
                handleAuthExpired(notify = true, message = error.feedErrorMessage())
            } catch (error: Exception) {
                ensureActive()
                if (generations["collect"] == generation) {
                    mutableState.value = state.value.copy(
                        collections = before.copy(
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

    private fun loadCoinRankPage(append: Boolean) {
        coinRankJob?.cancel()
        val before = state.value.coinRank
        val page = if (append) coinRankNextPage else 1
        mutableState.value = state.value.copy(
            coinRank = before.copy(
                loading = !append && before.items.isEmpty(),
                refreshing = !append && before.items.isNotEmpty(),
                loadingMore = append,
                error = null,
                failedToLoadMore = false,
            ),
        )
        coinRankJob = scope.launch {
            try {
                val response = service.coinRank(page)
                ensureActive()
                val items = ((if (append) before.items else emptyList()) + response.datas)
                    .distinctBy { "${it.userId}:${it.rank}:${it.username}" }
                coinRankNextPage = page + 1
                mutableState.value = state.value.copy(
                    coinRank = CoinRankState(
                        items = items,
                        loaded = true,
                        hasMore = response.hasMorePages,
                        loadedPage = page,
                    ),
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                ensureActive()
                mutableState.value = state.value.copy(
                    coinRank = before.copy(
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

    private fun loadCoinHistoryPage(append: Boolean) {
        coinHistoryJob?.cancel()
        val before = state.value.coinHistory
        val page = if (append) coinHistoryNextPage else 1
        mutableState.value = state.value.copy(
            coinHistory = before.copy(
                loading = !append && before.items.isEmpty(),
                refreshing = !append && before.items.isNotEmpty(),
                loadingMore = append,
                error = null,
                failedToLoadMore = false,
            ),
        )
        coinHistoryJob = scope.launch {
            try {
                val response = service.coinHistory(page)
                ensureActive()
                val items = ((if (append) before.items else emptyList()) + response.datas)
                    .distinctBy { it.id }
                coinHistoryNextPage = page + 1
                mutableState.value = state.value.copy(
                    coinHistory = CoinHistoryState(
                        items = items,
                        loaded = true,
                        hasMore = response.hasMorePages,
                        loadedPage = page,
                    ),
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: AuthExpiredException) {
                handleAuthExpired(notify = true, message = error.feedErrorMessage())
            } catch (error: Exception) {
                ensureActive()
                mutableState.value = state.value.copy(
                    coinHistory = before.copy(
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

    private suspend fun fetchUserInfoInBackground() {
        try {
            val profile = service.userInfo()
            scope.coroutineContext[Job]?.ensureActive()
            applyUserProfile(profile.userInfo, profile.coinInfo)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: AuthExpiredException) {
            handleAuthExpired(notify = true, message = error.feedErrorMessage())
        } catch (_: Exception) {
            // Keep existing user info when optional refresh fails.
        }
    }

    private fun applyLoggedInUser(user: UserInfo, message: String) {
        val mergedCollected = (state.value.collectedIds + user.collectIds).distinct()
        mutableState.value = state.value.copy(
            user = user,
            authLoading = false,
            authError = null,
            collectedIds = mergedCollected,
            actionMessage = message,
        )
        syncCollectFlags(mergedCollected.toSet())
    }

    private fun applyUserProfile(user: UserInfo?, coinInfo: org.lazy.wanandroid.core.network.model.CoinInfo?) {
        val currentUser = user ?: state.value.user
        val collected = user?.collectIds?.distinct() ?: state.value.collectedIds
        mutableState.value = state.value.copy(
            user = currentUser,
            coinInfo = coinInfo ?: state.value.coinInfo,
            collectedIds = collected,
        )
        syncCollectFlags(collected.toSet())
    }

    private suspend fun handleAuthExpired(notify: Boolean, message: String = "登录状态已过期，请重新登录") {
        service.clearSavedSession()
        clearSessionState(if (notify) message else null)
    }

    private fun clearSessionState(message: String?) {
        mutableState.value = state.value.copy(
            user = null,
            coinInfo = null,
            authLoading = false,
            authError = null,
            collectedIds = emptyList(),
            collections = FeedState(),
            coinHistory = CoinHistoryState(),
            actionMessage = message,
        )
        syncCollectFlags(emptySet())
    }

    private fun updateCollectedState(newIds: List<Int>, targetArticleId: Int, collected: Boolean) {
        val idSet = newIds.toSet()
        mutableState.value = state.value.copy(
            collectedIds = newIds,
            home = state.value.home.withArticleCollect(targetArticleId, collected, idSet),
            plaza = state.value.plaza.withArticleCollect(targetArticleId, collected, idSet),
            projects = state.value.projects.withArticleCollect(targetArticleId, collected, idSet),
            search = state.value.search.withArticleCollect(targetArticleId, collected, idSet),
        )
    }

    private fun syncCollectFlags(collectedIds: Set<Int>) {
        mutableState.value = state.value.copy(
            home = state.value.home.syncCollects(collectedIds),
            plaza = state.value.plaza.syncCollects(collectedIds),
            projects = state.value.projects.syncCollects(collectedIds),
            search = state.value.search.syncCollects(collectedIds),
        )
    }

    private fun FeedState.withArticleCollect(targetId: Int, collected: Boolean, idSet: Set<Int>): FeedState = copy(
        articles = articles.map { article ->
            when {
                article.id == targetId -> article.copy(collect = collected)
                article.id != null -> article.copy(collect = article.id in idSet)
                else -> article
            }
        },
    )

    private fun FeedState.syncCollects(collectedIds: Set<Int>): FeedState = copy(
        articles = articles.map { article ->
            val id = article.id
            if (id == null) article else article.copy(collect = id in collectedIds)
        },
    )

    private fun saveSearchKeyword(keyword: String) {
        val updated = (listOf(keyword) + state.value.searchHistory.filterNot { it == keyword })
            .take(MAX_SEARCH_HISTORY)
        writeSearchHistory(updated)
        mutableState.value = state.value.copy(searchHistory = updated)
    }

    private fun readSearchHistory(): List<String> {
        val raw = storage.getString(SEARCH_HISTORY_KEY) ?: return emptyList()
        return runCatching { wireJson.decodeFromString<List<String>>(raw) }.getOrDefault(emptyList())
    }

    private fun writeSearchHistory(items: List<String>) {
        if (items.isEmpty()) {
            storage.putString(SEARCH_HISTORY_KEY, null)
        } else {
            storage.putString(SEARCH_HISTORY_KEY, wireJson.encodeToString(items))
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
