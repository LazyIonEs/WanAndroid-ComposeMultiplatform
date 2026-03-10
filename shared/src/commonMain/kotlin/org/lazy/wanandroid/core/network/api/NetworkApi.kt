package org.lazy.wanandroid.core.network.api

import org.lazy.wanandroid.getBaseUrl


object NetworkApi {

    /**
     * 首页文章列表
     *
     * ```
     * https://www.wanandroid.com/article/list/0/json
     *
     * 方法：GET
     * 参数：页码，拼接在连接中，从0开始。
     *
     * 注：该接口支持传入 page_size 控制分页数量，取值为[1-40]，不传则使用默认值，一旦传入了 page_size，后续该接口分页都需要带上，否则会造成分页读取错误。
     * ```
     */
    fun articleList(page: Int) = getBaseUrl() + "article/list/${page}/json"

    /**
     * 首页置顶文章
     * ```
     * https://www.wanandroid.com/article/top/json
     *
     * 方法：GET
     * ```
     */
    fun articleTop() = getBaseUrl() + "article/top/json"
}