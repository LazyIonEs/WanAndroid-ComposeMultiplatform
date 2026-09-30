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
    private val http = createNetworkClient()
    private val store = FeedStore(NetworkDataSource(http, platformBaseUrl()), scope)
    val state: StateFlow<WanAndroidState> = store.state

    fun start() {
        scope.launch {
            if (!state.value.home.loaded && !state.value.home.loading) store.refresh("home")
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
