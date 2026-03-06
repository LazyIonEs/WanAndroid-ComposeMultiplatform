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
import androidx.compose.material3.adaptive.navigationsuite.rememberNavigationSuiteScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.ui.NavDisplay
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.core.annotation.KoinExperimentalAPI
import org.lazy.wanandroid.di.initKoin
import org.lazy.wanandroid.feature.rememberAppState
import org.lazy.wanandroid.navigation.LocalNavigator
import org.lazy.wanandroid.navigation.Navigator
import org.lazy.wanandroid.navigation.TOP_LEVEL_NAV_ITEMS
import org.lazy.wanandroid.navigation.toEntries

@OptIn(KoinExperimentalAPI::class)
@Composable
fun App(windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo()) {
    MaterialTheme {
        val appState = rememberAppState()

        val navigator = remember { Navigator(appState.navigationState) }

        val navigatorState = rememberNavigationSuiteScaffoldState()

        val layoutType =
            NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(windowAdaptiveInfo)

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
            layoutType = layoutType,
            state = navigatorState
        ) {
            Scaffold(
                snackbarHost = {

                }
            ) { innerPadding ->
                CompositionLocalProvider(LocalNavigator provides navigator) {
                    NavDisplay(
                        entries = appState.navigationState.toEntries(koinEntryProvider()),
                        modifier = Modifier.fillMaxSize().padding(innerPadding),
                        onBack = { navigator.goBack() }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun AppPreview() {
    initKoin()
    App()
}