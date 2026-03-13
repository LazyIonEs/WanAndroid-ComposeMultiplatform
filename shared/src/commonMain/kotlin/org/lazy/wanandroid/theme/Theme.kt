package org.lazy.wanandroid.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme
import org.lazy.wanandroid.core.data.model.DarkThemeConfig


@Composable
fun AppTheme(
    darkThemeConfig: DarkThemeConfig,
    typography: Typography = MaterialTheme.typography,
    content: @Composable () -> Unit
) {
    val useDarkTheme = when (darkThemeConfig) {
        DarkThemeConfig.FOLLOW_SYSTEM -> isSystemInDarkTheme()
        DarkThemeConfig.LIGHT -> false
        DarkThemeConfig.DARK -> true
    }

    val colorScheme = rememberDynamicColorScheme(
        seedColor = Color(0xFFC7FF83),
        isDark = useDarkTheme,
        specVersion = ColorSpec.SpecVersion.SPEC_2025,
        style = PaletteStyle.TonalSpot,
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}