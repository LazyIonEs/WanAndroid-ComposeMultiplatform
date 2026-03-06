package org.lazy.wanandroid.core.network.model

import kotlinx.serialization.Serializable

/**
 * 文章
 * ```
 * {
 *   "adminAdd": false,
 *   "apkLink": "",
 *   "audit": 1,
 *   "author": "",
 *   "canEdit": false,
 *   "chapterId": 502,
 *   "chapterName": "自助",
 *   "collect": false,
 *   "courseId": 13,
 *   "desc": "",
 *   "descMd": "",
 *   "envelopePic": "",
 *   "fresh": true,
 *   "host": "",
 *   "id": 31227,
 *   "isAdminAdd": false,
 *   "link": "https://juejin.cn/post/7612525280755056674",
 *   "niceDate": "11小时前",
 *   "niceShareDate": "11小时前",
 *   "origin": "",
 *   "prefix": "",
 *   "projectLink": "",
 *   "publishTime": 1772498716000,
 *   "realSuperChapterId": 493,
 *   "selfVisible": 0,
 *   "shareDate": 1772498716000,
 *   "shareUser": "panoogunker@gmail.com",
 *   "superChapterId": 494,
 *   "superChapterName": "广场Tab",
 *   "tags": [],
 *   "title": "Compose 进阶&mdash;巧用 GraphicsLayer",
 *   "type": 0,
 *   "userId": 164286,
 *   "visible": 1,
 *   "zan": 0
 * }
 * ```
 */
@Serializable
data class Article(
    val adminAdd: Boolean?,
    val apkLink: String?,
    val audit: Int?,
    val author: String?,
    val canEdit: Boolean?,
    val chapterId: Int?,
    val chapterName: String?,
    val collect: Boolean?,
    val courseId: Int?,
    val desc: String?,
    val descMd: String?,
    val envelopePic: String?,
    val fresh: Boolean?,
    val host: String?,
    val id: Int?,
    val isAdminAdd: Boolean?,
    val link: String,
    val niceDate: String?,
    val niceShareDate: String?,
    val origin: String?,
    val prefix: String?,
    val projectLink: String?,
    val publishTime: Long?,
    val realSuperChapterId: Int?,
    val selfVisible: Int?,
    val shareDate: Long?,
    val shareUser: String?,
    val superChapterId: Int?,
    val superChapterName: String?,
    val tags: List<Tag>?,
    val title: String,
    val type: Int?,
    val userId: Int?,
    val visible: Int?,
    val zan: Int?
)