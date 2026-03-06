package org.lazy.wanandroid.feature.wechat.navigation

import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import org.lazy.wanandroid.feature.wechat.WeChatScreen

@OptIn(KoinExperimentalAPI::class)
val weChatEntry = module {
    navigation<WeChatNavKey> { route ->
        WeChatScreen()
    }
}