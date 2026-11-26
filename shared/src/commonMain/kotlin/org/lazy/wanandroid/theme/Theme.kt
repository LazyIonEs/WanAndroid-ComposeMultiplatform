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
import com.materialkolor.material3.rememberDynamicColorScheme
import org.lazy.wanandroid.core.data.model.DarkThemeConfig

object WanAccents {
    val Gold = Color(0xFFD99B26)
    val Silver = Color(0xFF8E9BA8)
    val Bronze = Color(0xFFC07A4B)
    val HeartRed = Color(0xFFE5484D)
    val HotOrange = Color(0xFFEA580C)
    val EmeraldGlow = Color(0xFF2E7D5B)
}

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
            headlineMedium = typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                lineHeight = 35.sp,
                letterSpacing = (-0.3).sp,
            ),
            titleLarge = typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 21.sp,
                lineHeight = 29.sp,
            ),
            titleMedium = typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                lineHeight = 24.sp,
            ),
            titleSmall = typography.titleSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            ),
            bodyMedium = typography.bodyMedium.copy(
                fontSize = 14.sp,
                lineHeight = 22.sp,
            ),
            bodySmall = typography.bodySmall.copy(
                fontSize = 12.sp,
                lineHeight = 18.sp,
            ),
            labelLarge = typography.labelLarge.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            ),
        ),
        content = content
    )
}
