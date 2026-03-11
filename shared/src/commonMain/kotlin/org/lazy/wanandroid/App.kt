package org.lazy.wanandroid

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FabPosition
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
import androidx.compose.material3.Typography
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import androidx.window.core.layout.WindowSizeClass
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.core.annotation.KoinExperimentalAPI
import org.lazy.wanandroid.di.initKoin
import org.lazy.wanandroid.feature.AppState
import org.lazy.wanandroid.feature.navigation.SettingsNavKey
import org.lazy.wanandroid.feature.rememberAppState
import org.lazy.wanandroid.navigation.LocalNavigator
import org.lazy.wanandroid.navigation.Navigator
import org.lazy.wanandroid.navigation.TOP_LEVEL_NAV_ITEMS
import org.lazy.wanandroid.navigation.TOP_LEVEL_NAV_KEYS
import org.lazy.wanandroid.navigation.toEntries

@OptIn(
    KoinExperimentalAPI::class,
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class
)
@Composable
fun App(
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo(supportLargeAndXLargeWidth = true),
    typography: Typography = MaterialTheme.typography
) {

    val scheme = rememberDynamicColorScheme(
        seedColor = Color(0xFFC7FF83),
        isDark = isSystemInDarkTheme(),
        specVersion = ColorSpec.SpecVersion.SPEC_2025,
        style = PaletteStyle.Vibrant,
    )

    MaterialTheme(
        colorScheme = scheme,
        typography = typography,
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

        Scaffold(
            topBar = {
                TopAppBar(title = {
                    Text(text = "WanAndroid")
                }, actions = {
                    AnimatedVisibility(!enterSecondaryPage) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { }) {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = "搜索"
                                )
                            }
                            IconButton(onClick = { navigator.navigate(SettingsNavKey) }) {
                                Icon(
                                    imageVector = Icons.Rounded.Settings,
                                    contentDescription = "设置"
                                )
                            }
                        }
                    }
                }, navigationIcon = {
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
            },
            snackbarHost = {

            },
            floatingActionButton = {
                AnimatedVisibility(
                    visible = !enterSecondaryPage,
                    enter = slideInVertically(initialOffsetY = { it * 2 }),
                    exit = slideOutVertically(targetOffsetY = { it * 2 }),
                ) {
                    HorizontalFloatingToolbar(
                        expanded = true,
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
                                        CircleShape,
                                        CircleShape,
                                        CircleShape
                                    ),
                                    modifier = Modifier.height(56.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        AnimatedVisibility(
                                            visible = selected || wide,
                                            enter = expandHorizontally(motionScheme.defaultSpatialSpec()),
                                            exit = shrinkHorizontally(motionScheme.defaultSpatialSpec())
                                        ) {
                                            Crossfade(selected) {
                                                if (it) Icon(
                                                    modifier = Modifier.padding(end = ButtonDefaults.IconSpacing),
                                                    painter = painterResource(navItem.selectedIcon),
                                                    contentDescription = navItem.label
                                                )
                                                else Icon(
                                                    modifier = Modifier.padding(end = ButtonDefaults.IconSpacing),
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
                    sceneStrategy = listDetailStrategy,
                    onBack = { navigator.goBack() })
            }
        }
    }
}

val LocalWindowAdaptiveInfo = staticCompositionLocalOf<WindowAdaptiveInfo> {
    error("No WindowAdaptiveInfo provided")
}

val LocalAppState = staticCompositionLocalOf<AppState> {
    error("No AppState provided")
}

@Preview
@Composable
fun AppPreview() {
    initKoin()
    App()
}