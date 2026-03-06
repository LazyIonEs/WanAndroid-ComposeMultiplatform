package org.lazy.wanandroid.core.network.model

import kotlinx.serialization.Serializable

/**
 * 数据结构
 *
 * ```
 * errorCode = 0 代表执行成功，不建议依赖任何非0的 errorCode.
 * errorCode = -1001 代表登录失效，需要重新登录。
 * ```
 */
@Serializable
data class Result<T>(
    val data: T,
    val errorCode: Int,
    val errorMsg: String
) {
    fun isSuccess() = errorCode == 0
}