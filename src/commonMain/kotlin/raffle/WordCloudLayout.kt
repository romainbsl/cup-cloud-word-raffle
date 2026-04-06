package raffle

import androidx.compose.ui.graphics.Color
import net.kodein.theme.KodeinColors
import net.kodein.theme.compose.Color
import kotlin.random.Random

/**
 * Immutable description of where and how a single word is rendered in the cloud.
 *
 * All spatial values are normalized to **0..1** so they stay resolution-independent;
 * actual pixel positions are computed at draw time inside [ParallaxWord].
 *
 * @property word       Display text.
 * @property x          Horizontal position (0 = left edge, 1 = right edge).
 * @property y          Vertical position (0 = top edge, 1 = bottom edge).
 * @property fontSize   Font size in **sp** (range ≈ 10–28).
 * @property alpha      Base opacity (range 0.2–0.75). Further modulated during reveal.
 * @property depth      Parallax depth factor (0 = far / slow, 1 = near / fast).
 * @property color      Text color.
 */
data class WordPlacement(
    val word: String,
    val x: Float,
    val y: Float,
    val fontSize: Float,
    val alpha: Float,
    val depth: Float,
    val color: Color,
)

private val wordColor = Color(KodeinColors.light)

/**
 * Deterministically generates a [WordPlacement] for each word using
 * **Mitchell's best-candidate** algorithm.
 *
 * For every word, [candidates] random positions are sampled and the one
 * that maximises the minimum squared distance to all previously placed
 * words is kept. This produces a well-spread, visually pleasing layout
 * without expensive force-simulation.
 *
 * Because the [Random] instance is seeded with [seed], the same seed
 * always produces the same layout — which enables smooth animated
 * transitions between layouts by simply changing the seed.
 *
 * @param words Full list of words (typically [data.RaffleData.words] + [data.RaffleData.participants]).
 * @param seed  Deterministic seed for reproducible layouts.
 * @return One [WordPlacement] per input word, in the same order.
 */
fun generateWordPlacements(words: List<String>, seed: Int): List<WordPlacement> {
    val random = Random(seed)
    val candidates = 15
    val placed = mutableListOf<Pair<Float, Float>>()

    return words.map { word ->
        var bestX = 0.05f + random.nextFloat() * 0.9f
        var bestY = 0.05f + random.nextFloat() * 0.9f
        var bestMinDist = minDistTo(bestX, bestY, placed)

        repeat(candidates - 1) {
            val cx = 0.05f + random.nextFloat() * 0.9f
            val cy = 0.05f + random.nextFloat() * 0.9f
            val d = minDistTo(cx, cy, placed)
            if (d > bestMinDist) {
                bestX = cx
                bestY = cy
                bestMinDist = d
            }
        }

        placed += bestX to bestY

        WordPlacement(
            word = word,
            x = bestX,
            y = bestY,
            fontSize = 10f + random.nextFloat() * 18f,
            alpha = 0.2f + random.nextFloat() * 0.55f,
            depth = random.nextFloat(),
            color = wordColor,
        )
    }
}

/** Minimum squared distance from ([x], [y]) to any point in [placed]. */
private fun minDistTo(x: Float, y: Float, placed: List<Pair<Float, Float>>): Float {
    if (placed.isEmpty()) return Float.MAX_VALUE
    return placed.minOf { (px, py) ->
        val dx = x - px
        val dy = y - py
        dx * dx + dy * dy
    }
}
