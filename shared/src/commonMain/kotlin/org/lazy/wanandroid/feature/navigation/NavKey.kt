package org.lazy.wanandroid.feature.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import org.lazy.wanandroid.core.network.model.Article

@Serializable
data object HomeNavKey : NavKey

@Serializable
data object PlazaNavKey : NavKey

@Serializable
data object ProjectNavKey : NavKey

@Serializable
data object SettingsNavKey : NavKey

@Serializable
data class ArticleNavKey(val article: Article) : NavKey