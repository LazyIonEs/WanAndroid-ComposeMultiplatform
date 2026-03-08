package org.lazy.wanandroid

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.js.Js
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.FontResource
import wanandroid.shared.generated.resources.NotoSansSC_Bold
import wanandroid.shared.generated.resources.Res

class WasmPlatform : Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

actual fun getBaseUrl(): String = "/api/"

actual fun httpClient(config: HttpClientConfig<*>.() -> Unit) = HttpClient(Js) {
    config(this)
}

actual fun getPlatformFontResource(): FontResource? = Res.font.NotoSansSC_Bold

@Composable
actual fun getPlatformSpecificFontFamily(): FontFamily? = FontFamily(
    Font(Res.font.NotoSansSC_Bold, FontWeight.Normal),
    Font(Res.font.NotoSansSC_Bold, FontWeight.Bold),
)