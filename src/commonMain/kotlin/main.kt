import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import data.RaffleData
import net.kodein.cup.*
import net.kodein.cup.laser.laser
import net.kodein.cup.speaker.speakerWindow
import net.kodein.theme.KodeinColors
import net.kodein.theme.compose.Color
import net.kodein.theme.compose.KodeinVectorImages
import net.kodein.theme.compose.img.KodeinMonogram
import net.kodein.theme.cup.KodeinCupMaterialTheme
import net.kodein.theme.cup.KodeinPresentationBackground
import net.kodein.theme.cup.slides.kodeinActivities
import raffle.raffle
import raffle.raffleState
import slides.wordCloudRaffle
import slides.introSlide
import slides.outroSlide

private val slides = Slides(
    introSlide,
    kodeinActivities,
    wordCloudRaffle,
    outroSlide,
)

fun main() = cupApplication(title = "KotlinConf Conference Pass Giveaway") {
    KodeinCupMaterialTheme {
        Presentation(
            slides = slides,
            configuration = {
                speakerWindow()
                laser()
                raffle(raffleState, RaffleData.participants)
            },
            backgroundColor = MaterialTheme.colors.background
        ) { slidesContent ->
            // Kodein monogram watermark
            Image(
                painter = rememberVectorPainter(KodeinVectorImages.Logo.KodeinMonogram),
                contentDescription = null,
                alignment = Alignment.CenterEnd,
                colorFilter = ColorFilter.tint(Color(KodeinColors.dark_purple)),
                modifier = Modifier
                    .alpha(0.5f)
                    .fillMaxSize()
                    .offset(x = (-16).dp, y = 64.dp)
            )

            // Per-slide animated background color overlay
            val presentationState = LocalPresentationState.current
            val overBackground by animateColorAsState(
                targetValue = presentationState.currentSlide.user[KodeinPresentationBackground]?.color
                    ?: Color.Transparent,
                animationSpec = tween(1_500),
                label = "overBackground"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(overBackground)
            ) {
                Box(modifier = Modifier.padding(8.dp)) {
                    CompositionLocalProvider(
                        LocalContentColor provides MaterialTheme.colors.onBackground
                    ) {
                        slidesContent()
                    }
                }
                // Progress bar
                ProgressBar(
                    presentationState = presentationState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@Composable
private fun ProgressBar(presentationState: PresentationState, modifier: Modifier = Modifier) {
    val totalStepCount = presentationState.slides.sumOf { it.stepCount }
    val currentStepCount = presentationState.slides.subList(0, presentationState.currentSlideIndex)
        .sumOf { it.stepCount } + presentationState.currentStep

    Box(modifier = modifier) {
        val fraction by animateFloatAsState(
            targetValue = if (totalStepCount > 1) {
                currentStepCount.toFloat() / (totalStepCount - 1).toFloat()
            } else 1f,
            animationSpec = tween(300, easing = LinearOutSlowInEasing),
            label = "progressFraction"
        )
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraction = fraction)
                .align(Alignment.CenterStart)
                .background(Color(KodeinColors.dark_purple))
        )
    }
}
