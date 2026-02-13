package org.lazy.wanandroid.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DataUsage
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material.icons.rounded.DataUsage
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.ui.graphics.vector.ImageVector
import org.lazy.wanandroid.feature.home.navigation.HomeNavKey
import org.lazy.wanandroid.feature.navigation.navigation.NavigationNavKey
import org.lazy.wanandroid.feature.project.navigation.ProjectNavKey
import org.lazy.wanandroid.feature.settings.navigation.SettingsNavKey
import org.lazy.wanandroid.feature.wechat.navigation.WeChatNavKey

data class TopLevelNavItem(
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val iconText: String
)

val HOME = TopLevelNavItem(
    selectedIcon = Icons.Rounded.Home,
    unselectedIcon = Icons.Outlined.Home,
    iconText = "首页",
)

val NAVIGATION = TopLevelNavItem(
    selectedIcon = Icons.Rounded.Navigation,
    unselectedIcon = Icons.Outlined.Navigation,
    iconText = "导航",
)

val PROJECT = TopLevelNavItem(
    selectedIcon = Icons.Rounded.ViewAgenda,
    unselectedIcon = Icons.Outlined.ViewAgenda,
    iconText = "项目",
)

val WECHAT = TopLevelNavItem(
    selectedIcon = Icons.Rounded.DataUsage,
    unselectedIcon = Icons.Outlined.DataUsage,
    iconText = "公众号",
)

val SETTINGS = TopLevelNavItem(
    selectedIcon = Icons.Rounded.Settings,
    unselectedIcon = Icons.Outlined.Settings,
    iconText = "设置",
)

val TOP_LEVEL_NAV_ITEMS = mapOf(
    HomeNavKey to HOME,
    NavigationNavKey to NAVIGATION,
    ProjectNavKey to PROJECT,
    WeChatNavKey to WECHAT,
    SettingsNavKey to SETTINGS,
)