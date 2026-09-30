package org.lazy.wanandroid.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
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
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), shape = RoundedCornerShape(24.dp)) {
                    Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Rounded.AutoStories, null, Modifier.size(32.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("让阅读更合心意", style = MaterialTheme.typography.headlineMedium)
                        Text("选择舒服的外观，留一点时间给知识与灵感。", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            item { FeedSectionHeading("外观", "自动保存，下次打开继续使用") }
            item {
                Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
                    Column(Modifier.selectableGroup()) {
                        DarkThemeConfig.entries.forEachIndexed { index, option ->
                            val label = when (option) { DarkThemeConfig.FOLLOW_SYSTEM -> "跟随系统"; DarkThemeConfig.LIGHT -> "浅色模式"; DarkThemeConfig.DARK -> "深色模式" }
                            val description = when (option) { DarkThemeConfig.FOLLOW_SYSTEM -> "随设备外观自动切换"; DarkThemeConfig.LIGHT -> "明亮、清晰的阅读空间"; DarkThemeConfig.DARK -> "适合夜间的柔和深色" }
                            val icon = when (option) { DarkThemeConfig.FOLLOW_SYSTEM -> Icons.Rounded.BrightnessAuto; DarkThemeConfig.LIGHT -> Icons.Rounded.LightMode; DarkThemeConfig.DARK -> Icons.Rounded.DarkMode }
                            Row(
                                Modifier.fillMaxWidth().selectable(selected = theme == option, role = Role.RadioButton, onClick = { viewModel.saveAppThemeConfig(option) }).padding(18.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(label, style = MaterialTheme.typography.titleSmall)
                                    Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                RadioButton(selected = theme == option, onClick = null)
                            }
                            if (index < DarkThemeConfig.entries.lastIndex) HorizontalDivider(Modifier.padding(horizontal = 18.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        }
                    }
                }
            }
            item { FeedSectionHeading("阅读小贴士") }
            item {
                Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceContainerLow) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        ReadingTip(Icons.Rounded.Refresh, "刷新，发现新内容", "刷新列表获取新内容，滑到末尾会自动加载更多。")
                        ReadingTip(Icons.Rounded.TouchApp, "轻触，开始深入阅读", "点击文章或项目卡片，打开原文继续阅读。")
                    }
                }
            }
            item { FeedSectionHeading("关于 WanAndroid") }
            item {
                Card(
                    onClick = { uriHandler.openUri("https://wanandroid.com") },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                ) {
                    Row(Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Language, null, tint = MaterialTheme.colorScheme.primary)
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("玩 Android 开放社区", style = MaterialTheme.typography.titleSmall)
                            Text("文章与项目内容来自玩 Android", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.AutoMirrored.Rounded.OpenInNew, "在浏览器中打开", Modifier.size(20.dp))
                    }
                }
            }
            item {
                Text("保持好奇，持续进步。", modifier = Modifier.padding(vertical = 12.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ReadingTip(icon: ImageVector, title: String, description: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
