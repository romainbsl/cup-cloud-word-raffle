package slides

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp
import data.RaffleData
import kotlinx.coroutines.launch
import net.kodein.cup.SLIDE_SIZE_16_9
import net.kodein.cup.Slide
import net.kodein.cup.SlideSpecs
import raffle.ParallaxWord
import raffle.RaffleMode
import raffle.RaffleState
import raffle.RevealedParticipant
import raffle.generateWordPlacements
import raffle.raffleState

/**
 * CuP slide that hosts the interactive word-cloud raffle.
 *
 * This is a 16 : 9 slide containing [WordCloudContent] wired to the
 * global [raffleState].  All interaction (pressing **R**) is handled by
 * the [raffle.RafflePlugin]; this slide only renders the current state.
 */
val wordCloudRaffle by Slide(
    specs = SlideSpecs(size = SLIDE_SIZE_16_9)
) { _ ->
    Box(Modifier.fillMaxSize()) {
        WordCloudContent(raffleState)
    }
}

/**
 * Main composable for the word-cloud raffle.
 *
 * Renders every word from [RaffleData] as a [raffle.ParallaxWord] and overlays
 * a [raffle.RevealedParticipant] when a winner has been drawn.
 *
 * Visual effects:
 * - **Parallax** – words drift via a slow sinusoidal time loop (no mouse
 *   tracking). Shift magnitude scales with each word's [raffle.WordPlacement.depth].
 * - **Shuffle animation** – when [raffle.RaffleState.seed] changes, each word
 *   animates to its new position over 600 ms (FastOutSlowIn easing).
 * - **Dim on reveal** – all cloud words fade to 15 % opacity when a
 *   participant is revealed.
 */
@Composable
private fun WordCloudContent(state: RaffleState) {
    val scope = rememberCoroutineScope()

    val cloudWords = remember { RaffleData.words + RaffleData.participants }

    // Initial placements (updated on seed change)
    val initialPlacements = remember { generateWordPlacements(cloudWords, state.seed) }

    // Animatable positions per word (x, y) — initialized once
    val animX = remember { initialPlacements.map { Animatable(it.x) } }
    val animY = remember { initialPlacements.map { Animatable(it.y) } }

    // Static properties (don't change between shuffles — only layout position changes)
    val placements = remember { initialPlacements }

    // Animate to new positions on every seed change (except the very first)
    var lastSeed by remember { mutableIntStateOf(state.seed) }
    LaunchedEffect(state.seed) {
        if (state.seed == lastSeed) return@LaunchedEffect
        lastSeed = state.seed
        val newPlacements = generateWordPlacements(cloudWords, state.seed)
        newPlacements.forEachIndexed { i, p ->
            scope.launch {
                animX[i].animateTo(p.x, tween(600, easing = FastOutSlowInEasing))
            }
            scope.launch {
                animY[i].animateTo(p.y, tween(600, easing = FastOutSlowInEasing))
            }
        }
    }

    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    // Breathing animation (sinusoidal via linear repeating)
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val breathX by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(10_000, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "breathX"
    )
    val breathY by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(14_000, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "breathY"
    )

    val parallaxX = breathX
    val parallaxY = breathY

    // Word alpha (dims during reveal)
    val wordAlpha by animateFloatAsState(
        targetValue = if (state.mode == RaffleMode.Revealed) 0.15f else 1f,
        animationSpec = tween(600),
        label = "wordAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { containerSize = it }
    ) {
        val w = containerSize.width.toFloat()
        val h = containerSize.height.toFloat()

        if (w > 0f && h > 0f) {
            placements.forEachIndexed { i, placement ->
                ParallaxWord(
                    word = placement.word,
                    animX = animX[i].value,
                    animY = animY[i].value,
                    fontSize = placement.fontSize,
                    baseAlpha = placement.alpha,
                    depth = placement.depth,
                    color = placement.color,
                    parallaxOffsetX = parallaxX,
                    parallaxOffsetY = parallaxY,
                    wordAlphaMultiplier = wordAlpha,
                    containerWidth = w,
                    containerHeight = h,
                )
            }
        }

        RevealedParticipant(
            participant = state.currentParticipant,
        )

        Text(
            text = if (state.mode == RaffleMode.Revealed) "Press R to reset" else "Press R to reveal",
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .alpha(0.3f),
        )
    }
}
