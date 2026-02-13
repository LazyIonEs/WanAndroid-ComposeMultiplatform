package org.lazy.wanandroid.feature.wechat.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import org.lazy.wanandroid.feature.wechat.WeChatScreen

fun EntryProviderScope<NavKey>.weChatEntry() {
    entry<WeChatNavKey> {
        WeChatScreen()
    }
}