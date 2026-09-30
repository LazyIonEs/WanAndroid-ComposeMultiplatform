package org.lazy.wanandroid

import com.fleeksoft.ksoup.Ksoup
import org.lazy.wanandroid.core.network.model.Article
import org.lazy.wanandroid.core.network.model.ArticleList
import org.lazy.wanandroid.core.network.model.ProjectCategory

/** API paging and presentation rules shared by native bridges and Paging 3. */
fun articleFirstPage(section: String): Int = when (section) {
    "home", "plaza" -> 0
    "projects" -> 1
    else -> error("Unknown section: $section")
}

val ArticleList.hasMorePages: Boolean get() = !over && curPage < pageCount

val Article.feedKey: String get() = id?.let { "id:$it" } ?: "link:$link"

// Apply only to freshly received API data, never to already displayed articles.
fun Article.toDisplayArticle(): Article = copy(
    title = Ksoup.parse(title).text(),
    desc = desc?.let { Ksoup.parse(it).text() },
)

fun ProjectCategory.toDisplayCategory(): ProjectCategory = copy(name = Ksoup.parse(name).text())

fun Throwable.feedErrorMessage(): String = message?.takeIf { it.isNotBlank() }
    ?: "加载失败，请检查网络后重试"
