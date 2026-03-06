package org.lazy.wanandroid.core.network.model

import kotlinx.serialization.Serializable

/**
 * 首页文档列表
 * ```
 *  {
 *    "curPage": 1,
 *    "datas": [Article],
 *    "offset": 0,
 *    "over": false,
 *    "pageCount": 16479,
 *    "size": 1,
 *    "total": 16479
 *  }
 * ```
 */
@Serializable
data class ArticleList(
    val curPage: Int,
    val datas: List<Article>,
    val offset: Int,
    val over: Boolean,
    val pageCount: Int,
    val size: Int,
    val total: Int,
)