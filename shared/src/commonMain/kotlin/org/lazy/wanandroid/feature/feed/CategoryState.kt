package org.lazy.wanandroid.feature.feed

import org.lazy.wanandroid.core.network.model.ProjectCategory

data class CategoryState(
    val loading: Boolean = false,
    val error: String? = null,
    val items: List<ProjectCategory> = emptyList(),
    val selectedId: Int? = null,
    val loaded: Boolean = false,
)
