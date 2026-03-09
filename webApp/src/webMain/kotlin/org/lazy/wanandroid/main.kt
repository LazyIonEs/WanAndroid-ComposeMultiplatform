package org.lazy.wanandroid

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.ComposeViewport
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.configureWebResources
import org.jetbrains.compose.resources.preloadFont
import org.lazy.wanandroid.di.initKoin

val koin = initKoin()

@OptIn(ExperimentalComposeUiApi::class, ExperimentalResourceApi::class)
fun main() {
    configureWebResources {
        resourcePathMapping { path -> "./$path" }
    }
    ComposeViewport {
        val font by preloadFont(getPlatformFontResource() ?: return@ComposeViewport)
        var fontsFallbackInitialized by remember { mutableStateOf(false) }
        if (fontsFallbackInitialized) {
            App(typography = interTypography())
        }
        val fontFamilyResolver = LocalFontFamilyResolver.current
        LaunchedEffect(fontFamilyResolver, font) {
            if (font != null) {
                fontFamilyResolver.preload(FontFamily(font!!))
                fontsFallbackInitialized = true
            }
        }
    }
}


/**
 * Generates a [Typography] configuration using the "Inter" font family if available.
 *
 * This function attempts to retrieve a platform-specific font family and applies it
 * to all Material 3 typography styles. If the font family cannot be resolved,
 * it falls back to the default [MaterialTheme.typography].
 *
 * @return A [Typography] object customized with the Inter font family, or the default
 * typography if the font is not available.
 */
@Composable
private fun interTypography(): Typography {
    val fontResource = getPlatformFontResource() ?: return MaterialTheme.typography
    val interFont =
        FontFamily(Font(fontResource, FontWeight.Normal), Font(fontResource, FontWeight.Bold))
    return with(MaterialTheme.typography) {
        copy(
            displayLarge = displayLarge.copy(
                fontFamily = interFont, fontWeight = FontWeight.Bold
            ),
            displayMedium = displayMedium.copy(
                fontFamily = interFont, fontWeight = FontWeight.Bold
            ),
            displaySmall = displaySmall.copy(
                fontFamily = interFont, fontWeight = FontWeight.Bold
            ),
            headlineLarge = headlineLarge.copy(
                fontFamily = interFont, fontWeight = FontWeight.Bold
            ),
            headlineMedium = headlineMedium.copy(
                fontFamily = interFont, fontWeight = FontWeight.Bold
            ),
            headlineSmall = headlineSmall.copy(
                fontFamily = interFont, fontWeight = FontWeight.Bold
            ),
            titleLarge = titleLarge.copy(
                fontFamily = interFont, fontWeight = FontWeight.Bold
            ),
            titleMedium = titleMedium.copy(
                fontFamily = interFont, fontWeight = FontWeight.Bold
            ),
            titleSmall = titleSmall.copy(
                fontFamily = interFont, fontWeight = FontWeight.Bold
            ),
            labelLarge = labelLarge.copy(
                fontFamily = interFont, fontWeight = FontWeight.Normal
            ),
            labelMedium = labelMedium.copy(
                fontFamily = interFont, fontWeight = FontWeight.Normal
            ),
            labelSmall = labelSmall.copy(
                fontFamily = interFont, fontWeight = FontWeight.Normal
            ),
            bodyLarge = bodyLarge.copy(
                fontFamily = interFont, fontWeight = FontWeight.Normal
            ),
            bodyMedium = bodyMedium.copy(
                fontFamily = interFont, fontWeight = FontWeight.Normal
            ),
            bodySmall = bodySmall.copy(
                fontFamily = interFont, fontWeight = FontWeight.Normal
            ),
        )
    }
}