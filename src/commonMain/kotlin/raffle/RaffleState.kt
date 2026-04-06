package raffle

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * The two visual phases of the raffle:
 * - [Cloud] – the word cloud is fully visible, no winner shown.
 * - [Revealed] – a randomly picked participant is displayed on top of a dimmed cloud.
 */
enum class RaffleMode { Cloud, Revealed }

/**
 * Observable state that drives the raffle UI.
 *
 * All properties are backed by Compose mutable state so that any composable
 * reading them recomposes automatically when they change.
 *
 * @property mode Current visual phase ([RaffleMode.Cloud] or [RaffleMode.Revealed]).
 * @property currentParticipant The name of the drawn participant, or `null` when no draw is active.
 * @property seed Random seed used to compute word-cloud layout positions.
 *                 Changing it triggers a re-shuffle animation.
 */
class RaffleState {
    var mode: RaffleMode by mutableStateOf(RaffleMode.Cloud)
    var currentParticipant: String? by mutableStateOf(null)
    var seed: Int by mutableIntStateOf(42)
}

/** Application-wide singleton instance shared between the plugin and the slide. */
val raffleState = RaffleState()
