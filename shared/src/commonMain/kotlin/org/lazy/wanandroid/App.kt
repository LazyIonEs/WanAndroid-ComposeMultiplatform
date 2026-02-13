package org.lazy.wanandroid

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import org.lazy.wanandroid.feature.home.navigation.homeEntry
import org.lazy.wanandroid.feature.navigation.navigation.navigationEntry
import org.lazy.wanandroid.feature.project.navigation.projectEntry
import org.lazy.wanandroid.feature.rememberAppState
import org.lazy.wanandroid.feature.settings.navigation.settingsEntry
import org.lazy.wanandroid.feature.wechat.navigation.weChatEntry
import org.lazy.wanandroid.navigation.Navigator
import org.lazy.wanandroid.navigation.TOP_LEVEL_NAV_ITEMS
import org.lazy.wanandroid.navigation.toEntries

@Preview
@Composable
fun App(windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo()) {
    MaterialTheme {
        val appState = rememberAppState()

        val navigator = remember { Navigator(appState.navigationState) }

        val layoutType = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(windowAdaptiveInfo)

        NavigationSuiteScaffold(
            navigationSuiteItems = {
                TOP_LEVEL_NAV_ITEMS.forEach { (navKey, navItem) ->
                    val selected = navKey == appState.navigationState.currentTopLevelKey
                    val icon = if (selected) navItem.selectedIcon else navItem.unselectedIcon
                    item(selected = selected, onClick = { navigator.navigate(navKey) }, icon = {
                        Icon(
                            icon, contentDescription = navItem.iconText
                        )
                    }, label = {
                        Text(navItem.iconText)
                    })
                }
            },
            layoutType = layoutType
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onBackground,
                snackbarHost = {

                }
            ) { innerPadding ->

                val entryProvider = entryProvider {
                    homeEntry()
                    navigationEntry()
                    projectEntry()
                    weChatEntry()
                    settingsEntry()
                }

                NavDisplay(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    entries = appState.navigationState.toEntries(entryProvider),
                    onBack = { navigator.goBack() },
                )
            }
        }
    }
}