package org.lazy.wanandroid.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import com.materialkolor.ktx.harmonize
import kotlin.random.Random

@Composable
fun AmbientRow(
    primaryContainer: Color = MaterialTheme.colorScheme.primaryContainer,
    secondaryContainer: Color = MaterialTheme.colorScheme.secondaryContainer,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val randomBrush = remember {
        val newColor1 = primaryContainer
            .harmonize(Color.hsv(hue = Random.nextFloat() * 360f, saturation = 1f, value = 1f))
        val newColor2 = secondaryContainer
            .harmonize(Color.hsv(hue = Random.nextFloat() * 360f, saturation = 1f, value = 1f))
        val gradientColor = listOf(newColor1, newColor2)

        val tileMode = when (Random.nextInt(0, 3)) {
            0 -> TileMode.Clamp
            1 -> TileMode.Repeated
            2 -> TileMode.Mirror
            else -> TileMode.Decal
        }

        Brush.linearGradient(
            colors = gradientColor,
            tileMode = tileMode
        )
    }

    Row(
        modifier = modifier.background(brush = randomBrush),
        content = content
    )
}