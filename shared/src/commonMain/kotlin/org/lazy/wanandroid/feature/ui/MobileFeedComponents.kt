package org.lazy.wanandroid.feature.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.lazy.wanandroid.core.network.model.Article

@Composable
internal fun FeedIntroduction(section: String, onExplore: ((String) -> Unit)?) {
    Column(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            when (section) { "plaza" -> "社区广场"; "projects" -> "开源灵感库"; else -> "给开发者的阅读空间" },
            style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary,
        )
        Text(
            when (section) { "plaza" -> "交流，让灵感发生。"; "projects" -> "好项目，值得被发现。"; else -> "保持好奇，持续进步。" },
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            when (section) {
                "plaza" -> "来自开发者的实践、思考与新鲜分享。"
                "projects" -> "发现实用工具与开源作品，从好代码中学习。"
                else -> "读一篇好文章，让今天的积累成为明天的灵感。"
            },
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (section == "home" && onExplore != null) {
            Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ExploreCard("社区广场", "发现不同的解法", Icons.Rounded.Forum, Modifier.weight(1f)) { onExplore("plaza") }
                ExploreCard("开源项目", "从灵感到实践", Icons.Rounded.Code, Modifier.weight(1f)) { onExplore("projects") }
            }
        }
    }
}

@Composable
private fun ExploreCard(title: String, subtitle: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, null, Modifier.size(22.dp), tint = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
internal fun FeedSectionHeading(title: String, detail: String? = null) {
    FlowRow(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.semantics { heading() })
        if (detail != null) Text(detail, modifier = Modifier.align(Alignment.CenterVertically), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
internal fun ArticleCard(article: Article, isProject: Boolean, featured: Boolean = false, onTopicClick: (Article) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val foreground = if (featured) colors.onPrimaryContainer else colors.onSurface
    val secondary = if (featured) colors.onPrimaryContainer.copy(alpha = 0.8f) else colors.onSurfaceVariant
    val author = article.author?.takeIf { it.isNotBlank() } ?: article.shareUser?.takeIf { it.isNotBlank() } ?: "匿名分享"
    val date = article.niceDate?.takeIf { it.isNotBlank() } ?: article.niceShareDate.orEmpty()
    Card(
        onClick = { onTopicClick(article) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = if (featured) colors.primaryContainer else colors.surfaceContainerLow, contentColor = foreground),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (article.pinned) ArticleBadge("站内置顶", if (featured) foreground else colors.primary, Icons.Rounded.PushPin)
                if (article.fresh == true) ArticleBadge("新", if (featured) foreground else colors.tertiary)
                article.chapterName?.takeIf { it.isNotBlank() }?.let { ArticleBadge(it, secondary) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                if (isProject) Surface(shape = RoundedCornerShape(14.dp), color = colors.secondaryContainer) {
                    Icon(Icons.Rounded.Code, null, Modifier.padding(12.dp).size(24.dp), tint = colors.onSecondaryContainer)
                }
                Text(article.title, modifier = Modifier.weight(1f), style = if (featured) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium)
            }
            article.desc?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = secondary, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
            HorizontalDivider(color = foreground.copy(alpha = 0.08f))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(shape = CircleShape, color = if (featured) colors.primary.copy(alpha = 0.12f) else colors.secondaryContainer) {
                    Box(Modifier.size(30.dp), contentAlignment = Alignment.Center) {
                        Text(author.take(1).uppercase(), style = MaterialTheme.typography.labelMedium, color = if (featured) foreground else colors.onSecondaryContainer)
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(author, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (date.isNotBlank()) Text(date, style = MaterialTheme.typography.bodySmall, color = secondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, Modifier.size(18.dp), tint = secondary)
            }
        }
    }
}

@Composable
private fun ArticleBadge(text: String, color: Color, icon: ImageVector? = null) {
    Surface(color = color.copy(alpha = 0.08f), contentColor = color, shape = RoundedCornerShape(7.dp)) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (icon != null) Icon(icon, null, Modifier.size(12.dp))
            Text(text, style = MaterialTheme.typography.labelSmall)
        }
    }
}
