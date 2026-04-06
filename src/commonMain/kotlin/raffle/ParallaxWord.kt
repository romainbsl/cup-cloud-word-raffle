package raffle

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * Renders a single word in the cloud with depth-based parallax.
 *
 * The word is absolutely positioned at ([animX], [animY]) (normalized 0..1,
 * converted to pixels via [containerWidth]/[containerHeight]) and shifted
 * by a parallax translation proportional to [depth] and the current
 * breathing offset ([parallaxOffsetX], [parallaxOffsetY]).
 *
 * Alpha is the product of [baseAlpha] (intrinsic layout opacity) and
 * [wordAlphaMultiplier] (global dimming factor used when a winner is revealed).
 */
@Composable
fun ParallaxWord(
    word: String,
    animX: Float,           // current animated normalized x (0..1)
    animY: Float,           // current animated normalized y (0..1)
    fontSize: Float,        // sp
    baseAlpha: Float,       // intrinsic alpha from layout (0.3..1.0)
    depth: Float,           // 0..1
    color: Color,
    parallaxOffsetX: Float, // combined parallax offset -1..1
    parallaxOffsetY: Float,
    wordAlphaMultiplier: Float, // 0..1 — dims all words during reveal
    containerWidth: Float,  // px
    containerHeight: Float, // px
) {
    val maxParallaxPx = 120f

    Text(
        text = word,
        color = color,
        fontSize = fontSize.sp,
        fontWeight = FontWeight.Light,
        modifier = Modifier
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
                // Center the text on its anchor point
                val xPx = (animX * containerWidth - placeable.width / 2f).roundToInt()
                val yPx = (animY * containerHeight - placeable.height / 2f).roundToInt()
                layout(constraints.maxWidth, constraints.maxHeight) {
                    placeable.place(xPx, yPx)
                }
            }
            .graphicsLayer {
                translationX = parallaxOffsetX * depth * maxParallaxPx
                translationY = parallaxOffsetY * depth * maxParallaxPx
                alpha = baseAlpha * wordAlphaMultiplier
            }
    )
}
