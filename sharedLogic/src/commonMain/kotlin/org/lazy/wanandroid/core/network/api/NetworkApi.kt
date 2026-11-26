package org.lazy.wanandroid.core.network.api

object NetworkApi {
    const val BASE_URL = "https://wanandroid.com/"

    fun articleList(page: Int) = "article/list/$page/json"
    fun articleTop() = "article/top/json"
    fun banner() = "banner/json"
    fun hotKeys() = "hotkey/json"
    fun search(page: Int) = "article/query/$page/json"

    fun plazaList(page: Int) = "user_article/list/$page/json"
    fun projectCategories() = "project/tree/json"
    fun projectList(page: Int) = "project/list/$page/json"

    fun login() = "user/login"
    fun register() = "user/register"
    fun logout() = "user/logout/json"
    fun userInfo() = "user/lg/userinfo/json"

    fun collectList(page: Int) = "lg/collect/list/$page/json"
    fun collectArticle(id: Int) = "lg/collect/$id/json"
    fun collectExternalArticle() = "lg/collect/add/json"
    fun uncollectOriginId(id: Int) = "lg/uncollect_originId/$id/json"
    fun uncollectFromMine(id: Int) = "lg/uncollect/$id/json"

    fun coinUserInfo() = "lg/coin/userinfo/json"
    fun coinList(page: Int) = "lg/coin/list/$page/json"
    fun coinRank(page: Int) = "coin/rank/$page/json"
}
