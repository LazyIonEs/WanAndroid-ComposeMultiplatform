package org.lazy.wanandroid

import kotlinx.serialization.Serializable
import org.lazy.wanandroid.core.network.model.ProjectCategory

@Serializable
data class WanAndroidState(
    val home: FeedState = FeedState(),
    val plaza: FeedState = FeedState(),
    val projects: FeedState = FeedState(),
    val categories: List<ProjectCategory> = emptyList(),
    val selectedProjectId: Int? = null,
    val categoriesLoading: Boolean = false,
    val categoriesError: String? = null,
) {
    fun feed(section: String): FeedState = when (section) {
        "home" -> home
        "plaza" -> plaza
        "projects" -> projects
        else -> error("Unknown section: $section")
    }
}
