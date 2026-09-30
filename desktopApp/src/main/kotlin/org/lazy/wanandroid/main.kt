package org.lazy.wanandroid

import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.rememberWindowState
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import dev.nucleusframework.application.DecoratedWindow
import dev.nucleusframework.application.NucleusBackend
import dev.nucleusframework.application.nucleusApplication
import dev.nucleusframework.darkmodedetector.isSystemInDarkMode
import dev.nucleusframework.window.NucleusDecoratedWindowTheme
import dev.nucleusframework.window.TitleBar
import org.koin.core.context.GlobalContext
import org.koin.core.context.stopKoin
import org.lazy.wanandroid.core.data.model.DarkThemeConfig
import org.lazy.wanandroid.core.data.repository.PreferencesRepository
import org.lazy.wanandroid.di.initKoin

fun main(args: Array<String>) = nucleusApplication(args, backend = NucleusBackend.Tao) {
    // Bootstrap native-image and the single-instance check before creating shared services.
    val koin = remember { initKoin(); GlobalContext.get() }
    val preferences = remember { koin.get<PreferencesRepository>() }
    val viewModelOwner = remember {
        object : ViewModelStoreOwner {
            override val viewModelStore = ViewModelStore()
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            viewModelOwner.viewModelStore.clear()
            stopKoin()
        }
    }
    val appearance by preferences.darkThemeConfig.collectAsState(DarkThemeConfig.FOLLOW_SYSTEM)
    val systemDark = isSystemInDarkMode()
    val dark = when (appearance) {
        DarkThemeConfig.FOLLOW_SYSTEM -> systemDark
        DarkThemeConfig.LIGHT -> false
        DarkThemeConfig.DARK -> true
    }
    // One shared Compose Multiplatform / Material 3 interface on every desktop OS.
    // Nucleus supplies the base window and native WebView host, without toolkit themes.
    NucleusDecoratedWindowTheme(isDark = dark) {
        DecoratedWindow(
            onCloseRequest = ::exitApplication,
            state = rememberWindowState(width = 1200.dp, height = 800.dp),
            title = "WanAndroid",
            minimumSize = DpSize(640.dp, 560.dp),
        ) {
            TitleBar {
                Text(
                    "WanAndroid",
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = if (dark) Color.White else Color(0xFF252525),
                    fontSize = 13.sp,
                )
            }
            CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelOwner) {
                App()
            }
        }
    }
}
