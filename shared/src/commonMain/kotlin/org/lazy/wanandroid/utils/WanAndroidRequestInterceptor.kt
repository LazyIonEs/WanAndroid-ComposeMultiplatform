package org.lazy.wanandroid.utils

import dev.nucleusframework.webview.request.RequestInterceptor
import dev.nucleusframework.webview.request.WebRequest
import dev.nucleusframework.webview.request.WebRequestInterceptResult
import dev.nucleusframework.webview.web.WebViewNavigator


object WanAndroidRequestInterceptor : RequestInterceptor {

    override fun onInterceptUrlRequest(
        request: WebRequest,
        navigator: WebViewNavigator
    ): WebRequestInterceptResult {
        if (isTargetAppScheme(request.url)) {
            return WebRequestInterceptResult.Reject
        }
        return WebRequestInterceptResult.Allow
    }

    private fun isTargetAppScheme(url: String): Boolean {
        return url.startsWith("taobao://") ||
                url.startsWith("tbopen://") ||
                url.startsWith("tmall://") ||
                url.startsWith("openapp.jdbear://") ||
                url.startsWith("openapp.jdmall://") ||
                url.startsWith("pinduoduo://") ||
                url.startsWith("alipays://") ||
                url.startsWith("weixin://")
    }
}