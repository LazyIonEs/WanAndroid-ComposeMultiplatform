package org.lazy.wanandroid.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.AutoStories
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import org.lazy.wanandroid.core.data.model.DarkThemeConfig
import org.lazy.wanandroid.feature.ui.FeedSectionHeading

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = koinViewModel()) {
    val theme by viewModel.darkThemeConfig.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier.widthIn(max = 760.dp).fillMaxSize(),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(24.dp),
                ) {
                    Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Rounded.AutoStories, null, Modifier.size(32.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("让阅读更合心意", style = MaterialTheme.typography.headlineMedium)
                        Text("选择舒服的外观，留一点时间给知识与灵感。", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            item { FeedSectionHeading("外观", "自动保存，下次打开继续使用") }
            item {
                OutlinedCard(shape = RoundedCornerShape(22.dp)) {
                    Column(Modifier.selectableGroup()) {
                        DarkThemeConfig.entries.forEachIndexed { index, option ->
                            val label = when (option) {
                                DarkThemeConfig.FOLLOW_SYSTEM -> "跟随系统"
                                DarkThemeConfig.LIGHT -> "浅色模式"
                                DarkThemeConfig.DARK -> "深色模式"
                            }
                            val description = when (option) {
                                DarkThemeConfig.FOLLOW_SYSTEM -> "随设备外观自动切换"
                                DarkThemeConfig.LIGHT -> "明亮、清晰的阅读空间"
                                DarkThemeConfig.DARK -> "适合夜间的柔和深色"
                            }
                            val icon = when (option) {
                                DarkThemeConfig.FOLLOW_SYSTEM -> Icons.Rounded.BrightnessAuto
                                DarkThemeConfig.LIGHT -> Icons.Rounded.LightMode
                                DarkThemeConfig.DARK -> Icons.Rounded.DarkMode
                            }
                            ListItem(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = theme == option,
                                        role = Role.RadioButton,
                                        onClick = { viewModel.saveAppThemeConfig(option) },
                                    ),
                                leadingContent = { Icon(icon, null, tint = MaterialTheme.colorScheme.primary) },
                                headlineContent = { Text(label) },
                                supportingContent = { Text(description) },
                                trailingContent = { RadioButton(selected = theme == option, onClick = null) },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            )
                            if (index < DarkThemeConfig.entries.lastIndex) {
                                HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                            }
                        }
                    }
                }
            }
            item { FeedSectionHeading("阅读小贴士") }
            item {
                OutlinedCard(shape = RoundedCornerShape(22.dp)) {
                    Column {
                        ListItem(
                            leadingContent = { Icon(Icons.Rounded.Refresh, null, tint = MaterialTheme.colorScheme.primary) },
                            headlineContent = { Text("刷新，发现新内容") },
                            supportingContent = { Text("下拉列表获取最新文章，滑到末尾会自动加载更多。") },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        )
                        HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                        ListItem(
                            leadingContent = { Icon(Icons.Rounded.Favorite, null, tint = MaterialTheme.colorScheme.primary) },
                            headlineContent = { Text("收藏，随时温故知新") },
                            supportingContent = { Text("点击文章卡片或阅读页顶栏的爱心即可同步收藏。") },
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        )
                    }
                }
            }
            item { FeedSectionHeading("关于 WanAndroid") }
            item {
                OutlinedCard(
                    onClick = { uriHandler.openUri("https://wanandroid.com") },
                    shape = RoundedCornerShape(22.dp),
                ) {
                    ListItem(
                        leadingContent = { Icon(Icons.Rounded.Language, null, tint = MaterialTheme.colorScheme.primary) },
                        headlineContent = { Text("玩 Android 开放社区") },
                        supportingContent = { Text("接口与内容来自 wanandroid.com") },
                        trailingContent = { Icon(Icons.AutoMirrored.Rounded.OpenInNew, "在浏览器中打开") },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    )
                }
            }
            item {
                Text(
                    "保持好奇，持续进步。",
                    modifier = Modifier.padding(vertical = 12.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
