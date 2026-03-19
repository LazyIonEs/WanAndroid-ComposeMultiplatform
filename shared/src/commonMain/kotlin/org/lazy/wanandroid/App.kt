package org.lazy.wanandroid

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.Typography
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import androidx.window.core.layout.WindowSizeClass
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.KoinApplication
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.koinConfiguration
import org.lazy.wanandroid.di.appModule
import org.lazy.wanandroid.feature.AppState
import org.lazy.wanandroid.feature.navigation.SettingsNavKey
import org.lazy.wanandroid.feature.rememberAppState
import org.lazy.wanandroid.navigation.LocalNavigator
import org.lazy.wanandroid.navigation.Navigator
import org.lazy.wanandroid.navigation.TOP_LEVEL_NAV_ITEMS
import org.lazy.wanandroid.navigation.TOP_LEVEL_NAV_KEYS
import org.lazy.wanandroid.navigation.toEntries
import org.lazy.wanandroid.theme.AppTheme

@OptIn(
    KoinExperimentalAPI::class,
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class
)
@Composable
fun App(
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo(supportLargeAndXLargeWidth = true),
    typography: Typography = MaterialTheme.typography,
    viewModel: AppViewModel = koinViewModel()
) {

    val darkThemeConfig by viewModel.darkThemeConfig.collectAsState()

    AppTheme(
        darkThemeConfig = darkThemeConfig,
        typography = typography
    ) {
        val appState = rememberAppState()

        val navigator = remember { Navigator(appState.navigationState) }

        val windowSizeClass = windowAdaptiveInfo.windowSizeClass

        val wide =
            remember { windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND) }

        val enterSecondaryPage = !TOP_LEVEL_NAV_KEYS.contains(appState.navigationState.currentKey)

        val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>(
            directive = calculatePaneScaffoldDirective(windowAdaptiveInfo)
        )

        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                AppTopBar(scrollBehavior, enterSecondaryPage, navigator)
            },
            snackbarHost = {

            },
            floatingActionButton = {
                FloatingActionBar(enterSecondaryPage, appState, navigator, wide)
            },
            floatingActionButtonPosition = FabPosition.Center,
        ) { innerPadding ->
            CompositionLocalProvider(
                LocalNavigator provides navigator,
                LocalWindowAdaptiveInfo provides windowAdaptiveInfo,
                LocalAppState provides appState
            ) {
                NavDisplay(
                    entries = appState.navigationState.toEntries(koinEntryProvider()),
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    sceneStrategies = listOf(listDetailStrategy),
                    onBack = { navigator.goBack() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppTopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    enterSecondaryPage: Boolean,
    navigator: Navigator
) {
    TopAppBar(
        title = { Text(text = "WanAndroid") },
        scrollBehavior = scrollBehavior,
        actions = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimatedVisibility(!enterSecondaryPage) {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "搜索"
                        )
                    }
                }

                IconButton(onClick = { navigator.navigate(SettingsNavKey) }) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "设置"
                    )
                }
            }
        },
        navigationIcon = {
            AnimatedVisibility(
                visible = enterSecondaryPage,
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally(),
            ) {
                IconButton(onClick = { navigator.goBack() }) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowBackIosNew,
                        contentDescription = "返回"
                    )
                }
            }
        })
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun FloatingActionBar(
    enterSecondaryPage: Boolean,
    appState: AppState,
    navigator: Navigator,
    wide: Boolean
) {
    AnimatedVisibility(
        visible = !enterSecondaryPage,
        enter = slideInVertically(initialOffsetY = { it * 2 }),
        exit = slideOutVertically(targetOffsetY = { it * 2 }),
    ) {
        HorizontalFloatingToolbar(
            expanded = true,
            expandedShadowElevation = FloatingToolbarDefaults.ContainerExpandedElevationWithFab,
            content = {
                TOP_LEVEL_NAV_ITEMS.forEach { (navKey, navItem) ->
                    val selected = navKey == appState.navigationState.currentTopLevelKey
                    ToggleButton(
                        checked = selected,
                        onCheckedChange = { checked ->
                            if (checked) {
                                navigator.navigate(navKey)
                            }
                        },
                        shapes = ToggleButtonDefaults.shapes(
                            ToggleButtonDefaults.roundShape,
                            ToggleButtonDefaults.roundShape,
                            ToggleButtonDefaults.roundShape,
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AnimatedVisibility(
                                visible = selected || wide,
                                enter = expandHorizontally(motionScheme.defaultSpatialSpec()),
                                exit = shrinkHorizontally(motionScheme.defaultSpatialSpec())
                            ) {
                                Crossfade(selected) {
                                    if (it) Icon(
                                        modifier = Modifier.padding(end = ToggleButtonDefaults.IconSpacing)
                                            .size(ToggleButtonDefaults.IconSize),
                                        painter = painterResource(navItem.selectedIcon),
                                        contentDescription = navItem.label
                                    ) else Icon(
                                        modifier = Modifier.padding(end = ToggleButtonDefaults.IconSpacing)
                                            .size(ToggleButtonDefaults.IconSize),
                                        painter = painterResource(navItem.unselectedIcon),
                                        contentDescription = navItem.label
                                    )
                                }
                            }
                            Text(
                                text = navItem.label,
                                fontSize = 16.sp,
                                lineHeight = 24.sp,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Clip
                            )
                        }
                    }
                }
            })
    }
}

val LocalWindowAdaptiveInfo = staticCompositionLocalOf<WindowAdaptiveInfo> {
    error("No WindowAdaptiveInfo provided")
}

val LocalAppState = staticCompositionLocalOf<AppState> {
    error("No AppState provided")
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    KoinApplication(configuration = koinConfiguration {
        modules(appModule)
    }) {
        MaterialTheme {
            App()
        }
    }
}