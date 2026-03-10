package org.lazy.wanandroid.core.data.model

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.lazy.wanandroid.utils.toShape

data class IconShape(
    val shape: Shape,
    val padding: Dp = 4.dp,
    val iconSize: Dp = 24.dp
) {
    fun takeOrElseFrom(
        iconShapesList: List<IconShape>
    ): IconShape = if (this == Random) iconShapesList
        .filter { it != Random }
        .random()
    else this

    companion object {
        val Random by lazy {
            IconShape(
                shape = RectangleShape,
                padding = 0.dp,
                iconSize = 0.dp
            )
        }

        @OptIn(ExperimentalMaterial3ExpressiveApi::class)
        val entries: List<IconShape> by lazy {
            listOf(
                MaterialShapes.Circle,
                MaterialShapes.Square,
                MaterialShapes.Slanted,
                // MaterialShapes.Arch,
                // MaterialShapes.Fan,
                // MaterialShapes.Arrow,
                // MaterialShapes.SemiCircle,
                // MaterialShapes.Oval,
                // MaterialShapes.Pill,
                // MaterialShapes.Triangle,
                // MaterialShapes.Diamond,
                // MaterialShapes.ClamShell,
                MaterialShapes.Pentagon,
                MaterialShapes.Gem,
                MaterialShapes.Sunny,
                MaterialShapes.VerySunny,
                MaterialShapes.Cookie4Sided,
                MaterialShapes.Cookie6Sided,
                MaterialShapes.Cookie7Sided,
                MaterialShapes.Cookie9Sided,
                MaterialShapes.Cookie12Sided,
                // MaterialShapes.Ghostish,
                MaterialShapes.Clover4Leaf,
                MaterialShapes.Clover8Leaf,
                // MaterialShapes.Burst,
                MaterialShapes.SoftBurst,
                // MaterialShapes.Boom,
                // MaterialShapes.SoftBoom,
                MaterialShapes.Flower,
                // MaterialShapes.Puffy,
                // MaterialShapes.PuffyDiamond,
                // MaterialShapes.PixelCircle,
                // MaterialShapes.Bun,
                // MaterialShapes.Heart
            ).map {
                IconShape(it.toShape(), 10.dp, 24.dp)
            }
        }
    }
}