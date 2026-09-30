package org.lazy.wanandroid

import org.jetbrains.compose.resources.FontResource
import org.koin.core.module.Module

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun getPlatformFontResource(): FontResource?

const val SETTINGS_NAME = "WanAndroid-Lazy"

expect val platformModule: Module
