package org.lazy.wanandroid.core.network.model

import kotlinx.serialization.Serializable

@Serializable
data class Tag(
    val name: String? = null,
    val url: String? = null
)
