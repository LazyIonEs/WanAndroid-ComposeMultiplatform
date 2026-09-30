package org.lazy.wanandroid.core.data.source

import org.lazy.wanandroid.core.network.model.Article
import org.lazy.wanandroid.core.network.model.ArticleList
import org.lazy.wanandroid.core.network.model.ProjectCategory

interface ArticleService {
    suspend fun articles(section: String, page: Int, categoryId: Int?): ArticleList
    suspend fun topArticles(): List<Article>
    suspend fun categories(): List<ProjectCategory>
    fun close() {}
}
