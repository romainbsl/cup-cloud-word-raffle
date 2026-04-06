# KotlinConf Conference Pass Giveaway

A [Compose ur Pres (CuP)](https://github.com/KodeinKoders/CuP) presentation that draws a random participant name from an animated word cloud. Built with Kotlin Multiplatform and Jetpack Compose.

## How it works

The presentation contains two slides:

1. **Intro** -- KotlinConf 2026 pass giveaway description (2 days in Munich, 21-22 May).
2. **Word Cloud Raffle** -- a full-screen cloud of Kotlin-ecosystem keywords and participant names with parallax drift and a draw/reveal cycle.

---

## Deep Dive: The Word Cloud

The word cloud is the core visual component of this presentation. It renders ~140 words (40 Kotlin buzzwords + ~100 participant names) as a floating, parallax-animated cloud, and supports an interactive raffle draw. This section explains each layer in detail.

### 1. Data Layer -- `RaffleData`

All content is defined statically in `data/RaffleData.kt`:

```kotlin
object RaffleData {
    val participants: List<String> = listOf(
        "Adele Carpenter", "Akniyet Arysbayev", /* ... 100+ names */
    )

    val words: List<String> = listOf(
        "Coroutines", "Flow", "StateFlow", "Compose", "KMP",
        "Gradle", "Multiplatform", "suspend", "sealed", /* ... */
    )
}
```

The two lists are concatenated at the slide level to form the full word set:

```kotlin
val cloudWords = remember { RaffleData.words + RaffleData.participants }
```

### 2. Layout Engine -- Mitchell's Best-Candidate Algorithm

`raffle/WordCloudLayout.kt` converts a list of words into a list of `WordPlacement` values -- each describing where and how a word appears on screen.

#### The `WordPlacement` data class

```kotlin
data class WordPlacement(
    val word: String,       // Display text
    val x: Float,           // Horizontal position (0..1, normalized)
    val y: Float,           // Vertical position (0..1, normalized)
    val fontSize: Float,    // Font size in sp (10..28)
    val alpha: Float,       // Base opacity (0.2..0.75)
    val depth: Float,       // Parallax depth factor (0 = far/slow, 1 = near/fast)
    val color: Color,       // Text color
)
```

All spatial values are **normalized to 0..1** -- they are resolution-independent and only converted to pixels at render time.

#### The placement algorithm

```kotlin
fun generateWordPlacements(words: List<String>, seed: Int): List<WordPlacement> {
    val random = Random(seed)
    val candidates = 15
    val placed = mutableListOf<Pair<Float, Float>>()

    return words.map { word ->
        // Sample `candidates` random positions
        var bestX = 0.05f + random.nextFloat() * 0.9f
        var bestY = 0.05f + random.nextFloat() * 0.9f
        var bestMinDist = minDistTo(bestX, bestY, placed)

        repeat(candidates - 1) {
            val cx = 0.05f + random.nextFloat() * 0.9f
            val cy = 0.05f + random.nextFloat() * 0.9f
            val d = minDistTo(cx, cy, placed)
            if (d > bestMinDist) {
                bestX = cx; bestY = cy; bestMinDist = d
            }
        }

        placed += bestX to bestY

        WordPlacement(
            word = word,
            x = bestX, y = bestY,
            fontSize = 10f + random.nextFloat() * 18f,
            alpha = 0.2f + random.nextFloat() * 0.55f,
            depth = random.nextFloat(),
            color = wordColor,
        )
    }
}
```

**How it works:** For each word, the algorithm generates 15 candidate positions within a 5%-95% margin (to avoid clipping at edges). It picks the candidate that maximizes the minimum distance to all already-placed words. This is [Mitchell's best-candidate algorithm](https://bl.ocks.org/mbostock/1893974) -- a simple O(n * k) approach that produces well-spread, visually pleasing distributions without needing force-directed simulation.

The `minDistTo` helper computes squared Euclidean distance to avoid `sqrt`:

```kotlin
private fun minDistTo(x: Float, y: Float, placed: List<Pair<Float, Float>>): Float {
    if (placed.isEmpty()) return Float.MAX_VALUE
    return placed.minOf { (px, py) ->
        val dx = x - px; val dy = y - py
        dx * dx + dy * dy
    }
}
```

**Key property: determinism.** Because `Random(seed)` is used, the same seed always produces the exact same layout. This is what makes smooth shuffle animations possible -- changing the seed generates a new layout, and each word animates from its old position to its new one.

### 3. State Management -- `RaffleState`

The raffle has two visual phases, modeled as an enum:

```kotlin
enum class RaffleMode { Cloud, Revealed }
```

All state lives in a single observable class backed by Compose `mutableStateOf`:

```kotlin
class RaffleState {
    var mode: RaffleMode by mutableStateOf(RaffleMode.Cloud)
    var currentParticipant: String? by mutableStateOf(null)
    var seed: Int by mutableIntStateOf(42)
}

val raffleState = RaffleState()  // application-wide singleton
```

Any composable reading these properties recomposes automatically when they change. The singleton pattern ensures the CuP plugin and the slide composable always see the same state.

### 4. Keyboard Interaction -- `RafflePlugin`

CuP's window-level key handler consumes all key events unconditionally, so standard `Modifier.onKeyEvent` won't work. Instead, the raffle uses a `CupPlugin` which gets first chance at key events:

```kotlin
internal class RafflePlugin(
    private val state: RaffleState,
    private val participants: List<String>,
) : CupPlugin {

    override fun onKeyEvent(event: CupKeyEvent): Boolean {
        if (event.type != KeyEventType.KeyDown) return false
        if (event.key != Key.R) return false

        when (state.mode) {
            RaffleMode.Cloud -> {
                // Pick a random winner and shuffle the cloud
                state.currentParticipant = participants[Random.nextInt(participants.size)]
                state.seed = Random.nextInt()
                state.mode = RaffleMode.Revealed
            }
            RaffleMode.Revealed -> {
                // Clear the winner and shuffle again
                state.currentParticipant = null
                state.seed = Random.nextInt()
                state.mode = RaffleMode.Cloud
            }
        }
        return true  // consume the event
    }
}
```

The plugin is registered via a DSL extension function:

```kotlin
fun CupConfigurationBuilder.raffle(state: RaffleState, participants: List<String>) {
    plugin(RafflePlugin(state, participants))
}

// Usage in main.kt:
Presentation(
    slides = slides,
    configuration = {
        speakerWindow()
        laser()
        raffle(raffleState, RaffleData.participants)
    },
    // ...
)
```

### 5. Rendering -- `WordCloudRaffleSlide`

The slide composable (`slides/WordCloudRaffleSlide.kt`) orchestrates three animation layers:

#### 5a. Shuffle Animation (position transitions)

Each word has its own `Animatable` for X and Y. When `state.seed` changes, new placements are generated and every word animates to its new position:

```kotlin
val animX = remember { initialPlacements.map { Animatable(it.x) } }
val animY = remember { initialPlacements.map { Animatable(it.y) } }

LaunchedEffect(state.seed) {
    val newPlacements = generateWordPlacements(cloudWords, state.seed)
    newPlacements.forEachIndexed { i, p ->
        scope.launch { animX[i].animateTo(p.x, tween(600, easing = FastOutSlowInEasing)) }
        scope.launch { animY[i].animateTo(p.y, tween(600, easing = FastOutSlowInEasing)) }
    }
}
```

Each word's X and Y animate independently in their own coroutine, all launched concurrently. The result is a fluid 600ms shuffle where every word glides to its new position.

#### 5b. Parallax Breathing (continuous drift)

A slow sinusoidal oscillation makes the cloud feel alive. Two `infiniteTransition` floats oscillate between -1 and 1 at different speeds:

```kotlin
val breathX by infiniteTransition.animateFloat(
    initialValue = -1f, targetValue = 1f,
    animationSpec = infiniteRepeatable(
        tween(10_000, easing = EaseInOutSine), RepeatMode.Reverse
    ),
)
val breathY by infiniteTransition.animateFloat(
    initialValue = -1f, targetValue = 1f,
    animationSpec = infiniteRepeatable(
        tween(14_000, easing = EaseInOutSine), RepeatMode.Reverse
    ),
)
```

- **X axis:** 10-second cycle
- **Y axis:** 14-second cycle

The different periods prevent the motion from looking mechanical -- they create a [Lissajous-like](https://en.wikipedia.org/wiki/Lissajous_curve) pattern that never exactly repeats.

#### 5c. Dim on Reveal

When a winner is drawn, all cloud words fade to 15% opacity:

```kotlin
val wordAlpha by animateFloatAsState(
    targetValue = if (state.mode == RaffleMode.Revealed) 0.15f else 1f,
    animationSpec = tween(600),
)
```

#### Putting it together

Each word is rendered as a `ParallaxWord` composable inside a `Box`:

```kotlin
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
        containerWidth = containerSize.width.toFloat(),
        containerHeight = containerSize.height.toFloat(),
    )
}
```

### 6. Single Word Rendering -- `ParallaxWord`

`raffle/ParallaxWord.kt` handles absolute positioning and depth-based parallax for a single word:

```kotlin
@Composable
fun ParallaxWord(
    word: String,
    animX: Float, animY: Float,           // animated normalized position
    fontSize: Float,
    baseAlpha: Float,
    depth: Float,                          // 0 = far, 1 = near
    color: Color,
    parallaxOffsetX: Float, parallaxOffsetY: Float,  // -1..1
    wordAlphaMultiplier: Float,            // global dim factor
    containerWidth: Float, containerHeight: Float,
) {
    val maxParallaxPx = 120f

    Text(
        text = word,
        fontSize = fontSize.sp,
        fontWeight = FontWeight.Light,
        modifier = Modifier
            // Absolute positioning: center text on its anchor point
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
                val xPx = (animX * containerWidth - placeable.width / 2f).roundToInt()
                val yPx = (animY * containerHeight - placeable.height / 2f).roundToInt()
                layout(constraints.maxWidth, constraints.maxHeight) {
                    placeable.place(xPx, yPx)
                }
            }
            // Parallax + alpha via graphicsLayer (no recomposition)
            .graphicsLayer {
                translationX = parallaxOffsetX * depth * maxParallaxPx
                translationY = parallaxOffsetY * depth * maxParallaxPx
                alpha = baseAlpha * wordAlphaMultiplier
            }
    )
}
```

**Parallax math:** The translation is `offset * depth * 120px`. A word at `depth = 1` (near) shifts up to 120px; a word at `depth = 0` (far) doesn't move at all. This creates a layered, floating feel where near words drift more than far ones.

**Performance:** Both the absolute positioning (`layout`) and the parallax shift (`graphicsLayer`) avoid triggering recomposition. The `graphicsLayer` modifier applies transformations at the render layer level, so changing `translationX/Y` or `alpha` only requires a redraw -- not a re-measure or re-layout.

### 7. Winner Reveal -- `RevealedParticipant`

When a participant is drawn, a centered overlay appears with animated fade + bouncy scale:

```kotlin
@Composable
fun RevealedParticipant(participant: String?) {
    val alpha by animateFloatAsState(
        targetValue = if (participant != null) 1f else 0f,
        animationSpec = tween(600),
    )
    val scale by animateFloatAsState(
        targetValue = if (participant != null) 1f else 0.3f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
    )

    if (participant == null) return

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                this.alpha = alpha
                scaleX = scale; scaleY = scale
            }
    ) {
        Text(
            text = participant,
            style = MaterialTheme.typography.h1,
            color = MaterialTheme.colors.primary,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}
```

The spring with `DampingRatioMediumBouncy` creates a playful overshoot effect -- the name pops in slightly larger than its final size, then settles. Combined with the 600ms alpha fade-in and the cloud dimming to 15%, this creates a dramatic reveal moment.

---

## Raffle Interaction

| Key | Current mode | Effect |
|-----|-------------|--------|
| **R** | Cloud | Picks a random participant, shuffles the cloud, reveals the winner |
| **R** | Revealed | Clears the winner, shuffles the cloud back |
| **Arrow Right / Space** | -- | Next slide (standard CuP navigation) |
| **Arrow Left / Backspace** | -- | Previous slide |

## Architecture

```
src/commonMain/kotlin/
+-- main.kt                       # CuP application entry point & theme
+-- data/
|   +-- RaffleData.kt             # Static participant + keyword lists
+-- raffle/
|   +-- RaffleState.kt            # Observable state (mode, participant, seed)
|   +-- RafflePlugin.kt           # CuP plugin -- handles R key press
|   +-- WordCloudLayout.kt        # Deterministic layout (Mitchell's best-candidate)
|   +-- ParallaxWord.kt           # Single-word composable with depth parallax
|   +-- RevealedParticipant.kt    # Winner name overlay with animated reveal
+-- slides/
    +-- IntroSlide.kt             # Title slide
    +-- WordCloudRaffleSlide.kt   # CuP slide + composable orchestration
```

### Key modules

| File | Responsibility |
|------|---------------|
| `RaffleState` | Compose `mutableStateOf` holder. Shared singleton (`raffleState`) ensures the plugin and slide stay in sync. |
| `RafflePlugin` | CuP keyboard plugin. Toggles `RaffleState.mode` and re-seeds the layout on every press of **R**. |
| `WordCloudLayout` | Pure function `generateWordPlacements()` -- uses Mitchell's best-candidate algorithm (seeded `Random`) to spread words evenly without force simulation. |
| `WordCloudRaffleSlide` | Composes the cloud: animates positions on seed change, runs a sinusoidal time-loop for parallax drift, and renders the reveal overlay. |
| `ParallaxWord` | Positions a word in absolute coordinates with a depth-scaled parallax translation. |
| `RevealedParticipant` | Centered text overlay that fades/scales in with a bouncy spring when a winner is shown. |

## Running

```bash
# Desktop
./gradlew run
```

## Configuration

- **Participants** -- edit `RaffleData.participants` in `src/commonMain/kotlin/data/RaffleData.kt`.
- **Cloud words** -- edit `RaffleData.words` in the same file.
- **Plugins** -- the presentation also enables `speakerWindow` and `laser` (see `main.kt`).
