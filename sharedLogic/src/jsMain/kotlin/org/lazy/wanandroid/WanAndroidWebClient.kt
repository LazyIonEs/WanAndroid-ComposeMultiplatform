package org.lazy.wanandroid

import kotlin.js.JsExport

@JsExport
class WanAndroidWebClient {
    private val client = WanAndroidClient()

    fun start() = client.start()
    fun refresh(section: String) = client.refresh(section)
    fun loadMore(section: String) = client.loadMore(section)
    fun selectProject(categoryId: Int) = client.selectProject(categoryId)

    fun watch(onState: (String) -> Unit): () -> Unit {
        val subscription = client.watch(onState)
        return { subscription.close() }
    }

    fun close() = client.close()
}
