package org.lazy.wanandroid.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import org.lazy.wanandroid.feature.navigation.HomeNavKey
import org.lazy.wanandroid.feature.navigation.PlazaNavKey
import org.lazy.wanandroid.feature.navigation.ProjectNavKey

data class TopLevelNavItem(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: String,
)

val HOME = TopLevelNavItem(
    selectedIcon = Icons.Rounded.Home,
    unselectedIcon = Icons.Outlined.Home,
    label = "首页",
)

val PLAZA = TopLevelNavItem(
    selectedIcon = Icons.Rounded.Explore,
    unselectedIcon = Icons.Outlined.Explore,
    label = "广场",
)

val PROJECT = TopLevelNavItem(
    selectedIcon = Icons.Rounded.Widgets,
    unselectedIcon = Icons.Outlined.Widgets,
    label = "项目",
)

val TOP_LEVEL_NAV_ITEMS: Map<NavKey, TopLevelNavItem> = mapOf(
    HomeNavKey to HOME,
    PlazaNavKey to PLAZA,
    ProjectNavKey to PROJECT,
)

val TOP_LEVEL_NAV_KEYS: Set<NavKey> = TOP_LEVEL_NAV_ITEMS.keys
