package org.lazy.wanandroid.core.network.api

object NetworkApi {
    const val BASE_URL = "https://www.wanandroid.com/"
    fun articleList(page: Int) = "article/list/$page/json"
    fun articleTop() = "article/top/json"
    fun plazaList(page: Int) = "user_article/list/$page/json"
    fun projectCategories() = "project/tree/json"
    fun projectList(page: Int) = "project/list/$page/json"
}
