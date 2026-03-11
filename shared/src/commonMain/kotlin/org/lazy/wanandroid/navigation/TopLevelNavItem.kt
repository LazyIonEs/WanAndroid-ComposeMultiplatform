package org.lazy.wanandroid.navigation

import androidx.navigation3.runtime.NavKey
import org.jetbrains.compose.resources.DrawableResource
import org.lazy.wanandroid.feature.navigation.HomeNavKey
import org.lazy.wanandroid.feature.navigation.PlazaNavKey
import org.lazy.wanandroid.feature.navigation.ProjectNavKey
import wanandroid.shared.generated.resources.Res
import wanandroid.shared.generated.resources.home_24
import wanandroid.shared.generated.resources.home_24_sel
import wanandroid.shared.generated.resources.navigation_24
import wanandroid.shared.generated.resources.navigation_24_sel
import wanandroid.shared.generated.resources.project_24
import wanandroid.shared.generated.resources.project_24_sel

data class TopLevelNavItem(
    val selectedIcon: DrawableResource,
    val unselectedIcon: DrawableResource,
    val label: String
)

val HOME = TopLevelNavItem(
    selectedIcon = Res.drawable.home_24_sel,
    unselectedIcon = Res.drawable.home_24,
    label = "首页",
)

val PLAZA = TopLevelNavItem(
    selectedIcon = Res.drawable.navigation_24_sel,
    unselectedIcon = Res.drawable.navigation_24,
    label = "广场",
)

val PROJECT = TopLevelNavItem(
    selectedIcon = Res.drawable.project_24_sel,
    unselectedIcon = Res.drawable.project_24,
    label = "项目",
)

val TOP_LEVEL_NAV_ITEMS: Map<NavKey, TopLevelNavItem> = mapOf(
    HomeNavKey to HOME,
    PlazaNavKey to PLAZA,
    ProjectNavKey to PROJECT,
)

val TOP_LEVEL_NAV_KEYS: Set<NavKey> = TOP_LEVEL_NAV_ITEMS.keys