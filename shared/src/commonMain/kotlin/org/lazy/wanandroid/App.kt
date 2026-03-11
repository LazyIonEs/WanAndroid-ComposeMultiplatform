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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.AppBarRow
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.motionScheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Typography
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.ui.NavDisplay
import androidx.window.core.layout.WindowSizeClass
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.navigation3.koinEntryProvider
import org.koin.core.annotation.KoinExperimentalAPI
import org.lazy.wanandroid.di.initKoin
import org.lazy.wanandroid.feature.rememberAppState
import org.lazy.wanandroid.feature.settings.navigation.SettingsNavKey
import org.lazy.wanandroid.navigation.LocalNavigator
import org.lazy.wanandroid.navigation.Navigator
import org.lazy.wanandroid.navigation.TOP_LEVEL_NAV_ITEMS
import org.lazy.wanandroid.navigation.toEntries
import wanandroid.shared.generated.resources.Res
import wanandroid.shared.generated.resources.search_24
import wanandroid.shared.generated.resources.settings_24

@OptIn(
    KoinExperimentalAPI::class,
    ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun App(
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo(
        supportLargeAndXLargeWidth = true
    ), typography: Typography = MaterialTheme.typography
) {
    MaterialTheme(typography = typography) {

        val appState = rememberAppState()

        val navigator = remember { Navigator(appState.navigationState) }

        val wide =
            remember { windowAdaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) }

        val showBackIcon = SettingsNavKey == appState.navigationState.currentTopLevelKey

        Scaffold(
            topBar = {
                TopAppBar(title = {
                    Text(text = "WanAndroid")
                }, actions = {
                    AppBarRow {
                        clickableItem(
                            onClick = { },
                            icon = {
                                Icon(
                                    painter = painterResource(Res.drawable.search_24),
                                    contentDescription = "搜索"
                                )
                            },
                            label = "搜索",
                        )
                        clickableItem(
                            onClick = { navigator.navigate(SettingsNavKey) },
                            icon = {
                                Icon(
                                    painter = painterResource(Res.drawable.settings_24),
                                    contentDescription = "设置"
                                )
                            },
                            label = "搜索"
                        )
                    }
                }, navigationIcon = {
                    AnimatedVisibility(
                        visible = showBackIcon,
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
                    visible = !showBackIcon,
                    enter = slideInVertically(initialOffsetY = { it * 2 }),
                    exit = slideOutVertically(targetOffsetY = { it * 2 }),
                ) {
                    HorizontalFloatingToolbar(
                        expanded = true,
                        colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(
                            toolbarContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            toolbarContentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ),
                        content = {
                            TOP_LEVEL_NAV_ITEMS.forEach { (navKey, navItem) ->
                                val selected = if (showBackIcon) {
                                    val topLevelStack = appState.navigationState.topLevelStack
                                    navKey == topLevelStack.getOrNull(topLevelStack.lastIndex - 1)
                                } else {
                                    navKey == appState.navigationState.currentTopLevelKey
                                }
                                TooltipBox(
                                    positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                                        TooltipAnchorPosition.Above
                                    ),
                                    tooltip = { PlainTooltip { Text(text = navItem.label) } },
                                    state = rememberTooltipState(),
                                ) {
                                    ToggleButton(
                                        checked = selected, onCheckedChange = { checked ->
                                            if (checked) {
                                                navigator.navigate(navKey)
                                            }
                                        }, colors = ToggleButtonDefaults.toggleButtonColors(
                                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                            checkedContainerColor = MaterialTheme.colorScheme.tertiary,
                                            checkedContentColor = MaterialTheme.colorScheme.onTertiary
                                        ), shapes = ToggleButtonDefaults.shapes(
                                            CircleShape, CircleShape, CircleShape
                                        ), modifier = Modifier.height(56.dp)
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
                            }
                        })
                }
            },
            floatingActionButtonPosition = FabPosition.Center,
        ) { innerPadding ->
            CompositionLocalProvider(LocalNavigator provides navigator) {
                NavDisplay(
                    entries = appState.navigationState.toEntries(koinEntryProvider()),
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    onBack = { navigator.goBack() })
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