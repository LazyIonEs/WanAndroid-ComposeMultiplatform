package org.lazy.wanandroid

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.lazy.wanandroid.di.initKoin

val koin = initKoin()

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "WanAndroid",
    ) {
        App()
    }
}