package org.lazy.wanandroid

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.core.layout.WindowSizeClass
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.KoinApplication
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.koinConfiguration
import org.lazy.wanandroid.di.appModule
import org.lazy.wanandroid.feature.AppState
import org.lazy.wanandroid.feature.navigation.ArticleNavKey
import org.lazy.wanandroid.feature.navigation.SettingsNavKey
import org.lazy.wanandroid.feature.rememberAppState
import org.lazy.wanandroid.navigation.*
import org.lazy.wanandroid.theme.AppTheme

@OptIn(KoinExperimentalAPI::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun App(
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfoV2(),
    typography: Typography = MaterialTheme.typography,
    viewModel: AppViewModel = koinViewModel(),
) {
    val darkThemeConfig by viewModel.darkThemeConfig.collectAsState()
    AppTheme(darkThemeConfig = darkThemeConfig, typography = typography) {
        val appState = rememberAppState()
        val navigator = remember { Navigator(appState.navigationState) }
        val currentKey = appState.navigationState.currentKey
        val secondaryPage = currentKey !in TOP_LEVEL_NAV_KEYS
        val wide = windowAdaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
        val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>(
            directive = calculatePaneScaffoldDirective(windowAdaptiveInfo)
        )
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            topBar = {
                TopAppBar(
                    title = {
                        if (secondaryPage) {
                            Text(
                                when (currentKey) {
                                    SettingsNavKey -> "设置"
                                    is ArticleNavKey -> currentKey.article.title
                                    else -> "WanAndroid"
                                },
                                maxLines = 1, overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.titleMedium,
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(12.dp)) {
                                    Icon(Icons.Rounded.Code, null, Modifier.padding(8.dp).size(22.dp), tint = MaterialTheme.colorScheme.onPrimary)
                                }
                                Text("WanAndroid", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    navigationIcon = {
                        if (secondaryPage) IconButton(onClick = navigator::goBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, "返回")
                        }
                    },
                    actions = {
                        if (!secondaryPage) IconButton(onClick = { navigator.navigate(SettingsNavKey) }) {
                            Icon(Icons.Rounded.Settings, "设置")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    scrollBehavior = scrollBehavior,
                )
            },
            floatingActionButton = {
                FloatingActionBar(secondaryPage, appState, navigator, wide)
            },
            floatingActionButtonPosition = FabPosition.Center,
        ) { innerPadding ->
            CompositionLocalProvider(
                LocalNavigator provides navigator,
                LocalWindowAdaptiveInfo provides windowAdaptiveInfo,
                LocalAppState provides appState,
            ) {
                NavDisplay(
                    entries = appState.navigationState.toEntries(koinEntryProvider()),
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    sceneStrategies = listOf(listDetailStrategy),
                    onBack = navigator::goBack,
                )
            }
        }
    }
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


val LocalWindowAdaptiveInfo = staticCompositionLocalOf<WindowAdaptiveInfo> { error("No WindowAdaptiveInfo provided") }
val LocalAppState = staticCompositionLocalOf<AppState> { error("No AppState provided") }

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    KoinApplication(configuration = koinConfiguration { modules(appModule) }) {
        MaterialTheme { App() }
    }
}
