package org.lazy.wanandroid

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class WanAndroidClientTest {
    @Test
    fun observersUseMainDispatcherAndClosingCancelsQueuedUiActions() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val client = WanAndroidClient()
        try {
            val snapshots = mutableListOf<String>()
            val subscription = client.watch { snapshots += it }
            // The caller need not be the UI thread; observation is dispatched to Main.
            assertTrue(snapshots.isEmpty())
            runCurrent()
            assertEquals(1, snapshots.size)

            client.refresh("plaza")
            client.close()
            runCurrent()
            client.refresh("projects")
            runCurrent()

            assertEquals(WanAndroidState(), client.state.value)
            assertEquals(1, snapshots.size)
            subscription.close()
        } finally {
            client.close()
            Dispatchers.resetMain()
        }
    }
}
