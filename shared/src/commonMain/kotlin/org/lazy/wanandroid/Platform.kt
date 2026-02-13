package org.lazy.wanandroid

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform