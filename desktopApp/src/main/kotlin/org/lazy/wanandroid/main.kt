package org.lazy.wanandroid

import androidx.compose.ui.ExperimentalComposeUiApi
import dev.nucleusframework.application.NucleusBackend
import dev.nucleusframework.application.nucleusApplication
import dev.nucleusframework.macoscompose.window.MacosDecoratedWindow
import org.lazy.wanandroid.di.initKoin

val koin = initKoin()

@OptIn(ExperimentalComposeUiApi::class)
fun main() = nucleusApplication(backend = NucleusBackend.Tao) {
    MacosDecoratedWindow(
        onCloseRequest = ::exitApplication,
        title = "WanAndroid",
    ) {
        App()
    }
}
