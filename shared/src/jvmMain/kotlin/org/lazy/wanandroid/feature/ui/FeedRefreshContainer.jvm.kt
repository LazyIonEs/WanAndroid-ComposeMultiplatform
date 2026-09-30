package org.lazy.wanandroid.feature.ui

import androidx.compose.foundation.ContextMenuArea
import androidx.compose.foundation.ContextMenuItem
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*

@Composable
internal actual fun FeedRefreshContainer(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    // Material pull-to-refresh depends on touch release/fling; desktop mouse wheels don't
    // provide that lifecycle. Use the same Compose context menu on every desktop OS.
    Box(modifier.onPreviewKeyEvent { event ->
        val refreshKey = event.key == Key.F5 || (event.key == Key.R && (event.isMetaPressed || event.isCtrlPressed))
        if (event.type == KeyEventType.KeyDown && refreshKey) {
            if (!isRefreshing) onRefresh()
            true
        } else false
    }.focusRequester(focusRequester).focusable()) {
        ContextMenuArea(items = { listOf(ContextMenuItem("刷新", enabled = !isRefreshing, onClick = onRefresh)) }) {
            Box(Modifier.fillMaxSize(), content = content)
        }
        if (isRefreshing) LinearProgressIndicator(Modifier.fillMaxWidth().align(Alignment.TopCenter))
    }
}
