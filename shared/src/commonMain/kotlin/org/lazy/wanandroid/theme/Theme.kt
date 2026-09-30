package org.lazy.wanandroid.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
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
        seedColor = Color(0xFF386A4B),
        isDark = useDarkTheme,
        specVersion = ColorSpec.SpecVersion.SPEC_2025,
        style = PaletteStyle.TonalSpot,
    )

    MaterialTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),
        typography = typography.copy(
            headlineMedium = typography.headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 38.sp),
            titleLarge = typography.titleLarge.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 31.sp),
            titleMedium = typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 26.sp),
            bodyMedium = typography.bodyMedium.copy(fontSize = 14.sp, lineHeight = 23.sp),
            labelLarge = typography.labelLarge.copy(fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
        ),
        content = content
    )
}
