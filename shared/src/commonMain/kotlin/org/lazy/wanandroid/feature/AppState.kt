package org.lazy.wanandroid.feature

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import org.lazy.wanandroid.feature.navigation.HomeNavKey
import org.lazy.wanandroid.navigation.NavigationState
import org.lazy.wanandroid.navigation.TOP_LEVEL_NAV_KEYS
import org.lazy.wanandroid.navigation.rememberNavigationState

@Composable
fun rememberAppState(
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
): AppState {
    val navigationState =
        rememberNavigationState(
            startKey = HomeNavKey,
            topLevelKeys = TOP_LEVEL_NAV_KEYS
        )

    return remember(
        navigationState,
        coroutineScope,
    ) {
        AppState(
            navigationState = navigationState,
            coroutineScope = coroutineScope,
        )
    }
}

@Stable
class AppState(
    val navigationState: NavigationState,
    coroutineScope: CoroutineScope,
) {

}