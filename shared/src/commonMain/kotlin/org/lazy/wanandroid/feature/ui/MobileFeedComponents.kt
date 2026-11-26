package org.lazy.wanandroid.feature.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.lazy.wanandroid.collectArticleId
import org.lazy.wanandroid.core.network.model.Article
import org.lazy.wanandroid.theme.WanAccents

@Composable
internal fun HomeQuickActionsRow(
    onOpenSearch: () -> Unit,
    onOpenCollect: () -> Unit,
    onOpenCoinRank: () -> Unit,
    onOpenProjects: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "search") {
            ElevatedAssistChip(
                onClick = onOpenSearch,
                label = { Text("热词搜索") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        modifier = Modifier.size(AssistChipDefaults.IconSize),
                    )
                },
            )
        }
        item(key = "collect") {
            ElevatedAssistChip(
                onClick = onOpenCollect,
                label = { Text("我的收藏") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(AssistChipDefaults.IconSize),
                        tint = WanAccents.HeartRed,
                    )
                },
            )
        }
        item(key = "coin") {
            ElevatedAssistChip(
                onClick = onOpenCoinRank,
                label = { Text("积分排行") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.EmojiEvents,
                        contentDescription = null,
                        modifier = Modifier.size(AssistChipDefaults.IconSize),
                        tint = WanAccents.Gold,
                    )
                },
            )
        }
        item(key = "projects") {
            ElevatedAssistChip(
                onClick = onOpenProjects,
                label = { Text("开源项目") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Widgets,
                        contentDescription = null,
                        modifier = Modifier.size(AssistChipDefaults.IconSize),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                },
            )
        }
    }
}

@Composable
internal fun FeedEditorialHeader(section: String) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = when (section) {
                "plaza" -> "社区动态"
                "projects" -> "开源灵感"
                else -> "每日精选"
            },
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = when (section) {
                "plaza" -> "发现开发者的最新分享"
                "projects" -> "精选开源项目与实践"
                else -> "保持好奇，持续进步"
            },
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.semantics { heading() },
        )
    }
}

@Composable
internal fun FeedSectionHeading(title: String, detail: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics { heading() },
        )
        if (detail != null) {
            Badge(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ) {
                Text(detail, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
            }
        }
    }
}

@Composable
internal fun ArticleCard(
    article: Article,
    isProject: Boolean,
    featured: Boolean = false,
    isCollected: Boolean = false,
    highlightQuery: String = "",
    onTopicClick: (Article) -> Unit,
    onToggleCollect: ((Article) -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    val foreground = if (featured) colors.onPrimaryContainer else colors.onSurface
    val secondary = if (featured) colors.onPrimaryContainer.copy(alpha = 0.78f) else colors.onSurfaceVariant
    val author = article.author?.takeIf { it.isNotBlank() } ?: article.shareUser?.takeIf { it.isNotBlank() } ?: "匿名分享"
    val date = article.niceDate?.takeIf { it.isNotBlank() } ?: article.niceShareDate.orEmpty()
    val hasCoverImage = isProject && !article.envelopePic.isNullOrBlank()

    val cardContent: @Composable ColumnScope.() -> Unit = {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                if (article.pinned) {
                    Badge(
                        containerColor = if (featured) colors.primary else colors.primaryContainer,
                        contentColor = if (featured) colors.onPrimary else colors.onPrimaryContainer,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            Icon(Icons.Rounded.PushPin, null, Modifier.size(11.dp))
                            Text("站内置顶")
                        }
                    }
                }
                if (article.fresh == true) {
                    Badge(
                        containerColor = WanAccents.HotOrange.copy(alpha = 0.14f),
                        contentColor = WanAccents.HotOrange,
                    ) {
                        Text("新", modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                    }
                }
                val chapterLabel = listOfNotNull(
                    article.superChapterName?.takeIf { it.isNotBlank() },
                    article.chapterName?.takeIf { it.isNotBlank() && it != article.superChapterName },
                ).joinToString(" · ")
                if (chapterLabel.isNotBlank()) {
                    Badge(
                        containerColor = colors.surfaceContainerHighest,
                        contentColor = secondary,
                    ) {
                        Text(chapterLabel, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                    }
                }
                article.tags?.take(2)?.forEach { tag ->
                    tag.name?.takeIf { it.isNotBlank() }?.let { tagName ->
                        Badge(
                            containerColor = colors.secondaryContainer,
                            contentColor = colors.onSecondaryContainer,
                        ) {
                            Text(tagName, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                        }
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    val titleStyle = if (featured) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium
                    if (highlightQuery.isNotBlank()) {
                        Text(
                            text = buildHighlightedText(article.title, highlightQuery, colors.primary),
                            style = titleStyle,
                        )
                    } else {
                        Text(text = article.title, style = titleStyle)
                    }
                    article.desc?.takeIf { it.isNotBlank() }?.let { desc ->
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodyMedium,
                            color = secondary,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                if (hasCoverImage) {
                    AsyncImage(
                        model = article.envelopePic,
                        contentDescription = article.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(width = 76.dp, height = 104.dp)
                            .clip(RoundedCornerShape(14.dp)),
                    )
                } else if (isProject) {
                    Icon(
                        imageVector = Icons.Rounded.Code,
                        contentDescription = null,
                        modifier = Modifier.padding(top = 4.dp).size(28.dp),
                        tint = colors.primary,
                    )
                }
            }

            HorizontalDivider(color = foreground.copy(alpha = 0.08f))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = secondary,
                )
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = author,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (date.isNotBlank()) {
                        Text(
                            text = date,
                            style = MaterialTheme.typography.bodySmall,
                            color = secondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                if (onToggleCollect != null && article.collectArticleId != null) {
                    IconToggleButton(
                        checked = isCollected,
                        onCheckedChange = { onToggleCollect(article) },
                        colors = IconButtonDefaults.iconToggleButtonColors(
                            checkedContentColor = WanAccents.HeartRed,
                            contentColor = secondary,
                        ),
                    ) {
                        Icon(
                            imageVector = if (isCollected) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                            contentDescription = if (isCollected) "取消收藏" else "收藏",
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = secondary,
                    )
                }
            }
        }
    }

    if (featured) {
        Card(
            onClick = { onTopicClick(article) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = colors.primaryContainer,
                contentColor = foreground,
            ),
            content = cardContent,
        )
    } else {
        OutlinedCard(
            onClick = { onTopicClick(article) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            content = cardContent,
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun FeedLoadingIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth().heightIn(min = 240.dp),
        contentAlignment = Alignment.Center,
    ) {
        ContainedLoadingIndicator()
    }
}

private fun buildHighlightedText(text: String, query: String, highlightColor: Color): AnnotatedString {
    val tokens = query.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    if (tokens.isEmpty()) return AnnotatedString(text)
    val lower = text.lowercase()
    val ranges = mutableListOf<IntRange>()
    for (token in tokens) {
        val t = token.lowercase()
        var index = lower.indexOf(t)
        while (index >= 0) {
            ranges += index until (index + t.length)
            index = lower.indexOf(t, index + t.length)
        }
    }
    if (ranges.isEmpty()) return AnnotatedString(text)
    val sorted = ranges.sortedBy { it.first }
    return buildAnnotatedString {
        var cursor = 0
        for (range in sorted) {
            if (range.first < cursor) continue
            if (range.first > cursor) append(text.substring(cursor, range.first))
            withStyle(SpanStyle(color = highlightColor, fontWeight = FontWeight.Bold)) {
                append(text.substring(range.first, range.last + 1))
            }
            cursor = range.last + 1
        }
        if (cursor < text.length) append(text.substring(cursor))
    }
}
