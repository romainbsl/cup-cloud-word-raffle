package raffle

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

/**
 * Full-screen overlay that displays the drawn [participant] name.
 *
 * The text fades in (600 ms tween) and scales up with a bouncy spring
 * when [visible] becomes `true`, and fades / shrinks back when it becomes `false`.
 * Nothing is composed when fully invisible (`alpha == 0`).
 */
@Composable
fun RevealedParticipant(
    participant: String?,
) {
    val alpha by animateFloatAsState(
        targetValue = if (participant != null) 1f else 0f,
        animationSpec = tween(600),
        label = "participantAlpha"
    )
    val scale by animateFloatAsState(
        targetValue = if (participant != null) 1f else 0.3f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "participantScale"
    )

    if (participant == null) return

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                this.alpha = alpha
                scaleX = scale
                scaleY = scale
            }
    ) {
        Text(
            text = participant,
            style = MaterialTheme.typography.h1,
            color = MaterialTheme.colors.primary,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
        )
    }
}
