package org.lazy.wanandroid

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.ToggleButtonSize
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Typography
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import androidx.window.core.layout.WindowSizeClass
import org.koin.compose.KoinApplication
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.koinConfiguration
import org.lazy.wanandroid.di.appModule
import org.lazy.wanandroid.feature.AppState
import org.lazy.wanandroid.feature.navigation.ArticleNavKey
import org.lazy.wanandroid.feature.navigation.AuthNavKey
import org.lazy.wanandroid.feature.navigation.CoinRankNavKey
import org.lazy.wanandroid.feature.navigation.CollectNavKey
import org.lazy.wanandroid.feature.navigation.PlazaNavKey
import org.lazy.wanandroid.feature.navigation.ProfileNavKey
import org.lazy.wanandroid.feature.navigation.ProjectNavKey
import org.lazy.wanandroid.feature.navigation.SearchNavKey
import org.lazy.wanandroid.feature.navigation.SettingsNavKey
import org.lazy.wanandroid.feature.rememberAppState
import org.lazy.wanandroid.navigation.LocalNavigator
import org.lazy.wanandroid.navigation.Navigator
import org.lazy.wanandroid.navigation.TOP_LEVEL_NAV_ITEMS
import org.lazy.wanandroid.navigation.TOP_LEVEL_NAV_KEYS
import org.lazy.wanandroid.navigation.toEntries
import org.lazy.wanandroid.theme.AppTheme

@OptIn(KoinExperimentalAPI::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun App(
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfoV2(),
    typography: Typography = MaterialTheme.typography,
    viewModel: AppViewModel = koinViewModel(),
) {
    val darkThemeConfig by viewModel.darkThemeConfig.collectAsState()
    val sessionState by viewModel.sessionState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.sessionEvents.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    AppTheme(darkThemeConfig = darkThemeConfig, typography = typography) {
        val appState = rememberAppState()
        val navigator = remember { Navigator(appState.navigationState) }
        val currentKey = appState.navigationState.currentKey
        val secondaryPage = currentKey !in TOP_LEVEL_NAV_KEYS
        val hideOuterTopBar = currentKey is SearchNavKey || currentKey is ArticleNavKey
        val wide = windowAdaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
        val listDetailStrategy = rememberListDetailSceneStrategy<NavKey>(
            directive = calculatePaneScaffoldDirective(windowAdaptiveInfo)
        )
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = if (!secondaryPage) Modifier.padding(bottom = 84.dp) else Modifier,
                )
            },
            topBar = {
                if (!hideOuterTopBar) {
                    TopAppBar(
                        title = {
                            if (secondaryPage) {
                                Text(
                                    when (currentKey) {
                                        SettingsNavKey -> "外观与设置"
                                        ProfileNavKey -> "个人中心"
                                        AuthNavKey -> "账号登录"
                                        CollectNavKey -> "我的收藏"
                                        is CoinRankNavKey -> "积分与排行"
                                        is ArticleNavKey -> currentKey.article.title
                                        else -> "WanAndroid"
                                    },
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.titleMedium,
                                )
                            } else {
                                val topTitle = when (currentKey) {
                                    PlazaNavKey -> "社区广场"
                                    ProjectNavKey -> "开源项目"
                                    else -> "WanAndroid"
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Code,
                                        contentDescription = null,
                                        modifier = Modifier.size(26.dp),
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                    AnimatedContent(targetState = topTitle) { title ->
                                        Text(
                                            title,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    }
                                }
                            }
                        },
                        navigationIcon = {
                            if (secondaryPage) IconButton(onClick = navigator::goBack) {
                                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "返回")
                            }
                        },
                        actions = {
                            if (!secondaryPage) {
                                IconButton(onClick = { navigator.navigate(SearchNavKey) }) {
                                    Icon(Icons.Rounded.Search, "搜索")
                                }
                                IconButton(onClick = { navigator.navigate(ProfileNavKey) }) {
                                    BadgedBox(
                                        badge = {
                                            if (sessionState.isLoggedIn) {
                                                Badge(containerColor = MaterialTheme.colorScheme.primary)
                                            }
                                        },
                                    ) {
                                        Icon(
                                            imageVector = if (sessionState.isLoggedIn) Icons.Rounded.AccountCircle else Icons.Outlined.AccountCircle,
                                            contentDescription = "个人中心",
                                        )
                                    }
                                }
                                IconButton(onClick = { navigator.navigate(SettingsNavKey) }) {
                                    Icon(Icons.Rounded.Settings, "设置")
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                        scrollBehavior = scrollBehavior,
                    )
                }
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
                        shapes = ToggleButtonDefaults.shapesFor(ToggleButtonSize.ExtraLarge)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AnimatedVisibility(
                                visible = selected || wide,
                                enter = expandHorizontally(motionScheme.defaultSpatialSpec()),
                                exit = shrinkHorizontally(motionScheme.defaultSpatialSpec())
                            ) {
                                Crossfade(selected) { isSelected ->
                                    Icon(
                                        modifier = Modifier
                                            .padding(end = ToggleButtonDefaults.IconSpacing)
                                            .size(ToggleButtonDefaults.IconSize),
                                        imageVector = if (isSelected) navItem.selectedIcon else navItem.unselectedIcon,
                                        contentDescription = navItem.label,
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
