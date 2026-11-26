package org.lazy.wanandroid

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import org.lazy.wanandroid.core.data.source.NetworkDataSource

class StateSubscription internal constructor(private val job: Job) {
    fun close() = job.cancel()
}

/** Native bridge: no Compose, AndroidX, or platform UI dependencies. */
class WanAndroidClient {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val storage = platformSessionStorage()
    private val cookieStorage = PersistentCookiesStorage(storage)
    private val http = createNetworkClient(cookieStorage)
    private val store = FeedStore(NetworkDataSource(http, platformBaseUrl(), cookieStorage), scope, storage)
    val state: StateFlow<WanAndroidState> = store.state

    fun start() {
        scope.launch {
            if (!state.value.home.loaded && !state.value.home.loading) store.refresh("home")
            store.restoreSession()
            store.loadHotKeys()
        }
    }

    fun refresh(section: String) {
        scope.launch { store.refresh(section) }
    }

    fun loadMore(section: String) {
        scope.launch { store.loadMore(section) }
    }

    fun selectProject(categoryId: Int) {
        scope.launch { store.selectProject(categoryId) }
    }

    fun loadHotKeys() {
        scope.launch { store.loadHotKeys() }
    }

    fun search(keyword: String) {
        scope.launch { store.search(keyword) }
    }

    fun loadMoreSearch() {
        scope.launch { store.loadMoreSearch() }
    }

    fun clearSearch() {
        scope.launch { store.clearSearch() }
    }

    fun removeSearchHistory(keyword: String) {
        scope.launch { store.removeSearchHistory(keyword) }
    }

    fun clearSearchHistory() {
        scope.launch { store.clearSearchHistory() }
    }

    fun login(username: String, password: String) {
        scope.launch { store.login(username, password) }
    }

    fun register(username: String, password: String, repassword: String) {
        scope.launch { store.register(username, password, repassword) }
    }

    fun logout() {
        scope.launch { store.logout() }
    }

    fun refreshUserInfo() {
        scope.launch { store.refreshUserInfo() }
    }

    fun clearAuthError() {
        scope.launch { store.clearAuthError() }
    }

    fun clearActionMessage() {
        scope.launch { store.clearActionMessage() }
    }

    fun toggleCollect(articleId: Int) {
        scope.launch { store.toggleCollect(articleId) }
    }

    fun refreshCollections() {
        scope.launch { store.refreshCollections() }
    }

    fun loadMoreCollections() {
        scope.launch { store.loadMoreCollections() }
    }

    fun uncollectFromMine(id: Int, originId: Int) {
        scope.launch { store.uncollectFromMine(id, originId) }
    }

    fun collectExternal(title: String, author: String, link: String) {
        scope.launch { store.collectExternal(title, author, link) }
    }

    fun refreshCoinRank() {
        scope.launch { store.refreshCoinRank() }
    }

    fun loadMoreCoinRank() {
        scope.launch { store.loadMoreCoinRank() }
    }

    fun refreshCoinHistory() {
        scope.launch { store.refreshCoinHistory() }
    }

    fun loadMoreCoinHistory() {
        scope.launch { store.loadMoreCoinHistory() }
    }

    fun watch(onState: (String) -> Unit): StateSubscription = StateSubscription(
        scope.launch {
            state.collect { onState(wireJson.encodeToString(it)) }
        }
    )

    fun close() {
        scope.cancel()
        http.close()
    }
}
