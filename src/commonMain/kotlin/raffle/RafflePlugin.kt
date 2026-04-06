@file:OptIn(net.kodein.cup.PluginCupAPI::class)

package raffle

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import net.kodein.cup.CupKeyEvent
import net.kodein.cup.PluginCupAPI
import net.kodein.cup.PresentationState
import net.kodein.cup.config.CupAdditionalOverlay
import net.kodein.cup.config.CupConfigurationBuilder
import net.kodein.cup.config.CupConfigurationDsl
import net.kodein.cup.config.CupPlugin
import net.kodein.cup.key
import net.kodein.cup.type
import kotlin.random.Random

/**
 * CuP plugin that listens for the **R** key to toggle the raffle draw.
 *
 * Pressing **R** cycles between:
 * 1. **Cloud → Revealed** – picks a random participant from [participants],
 *    stores the name in [state], re-seeds the cloud layout so the words shuffle.
 * 2. **Revealed → Cloud** – clears the winner, re-seeds again to shuffle.
 *
 * The plugin intentionally renders no overlay of its own; the visual result is
 * driven entirely by the composables that read [RaffleState].
 */
internal class RafflePlugin(
    private val state: RaffleState,
    private val participants: List<String>,
) : CupPlugin {

    override fun onKeyEvent(event: CupKeyEvent): Boolean {
        if (event.type != KeyEventType.KeyDown) return false
        if (event.key != Key.R) return false

        when (state.mode) {
            RaffleMode.Cloud -> {
                state.currentParticipant = participants[Random.nextInt(participants.size)]
                state.seed = Random.nextInt()
                state.mode = RaffleMode.Revealed
            }
            RaffleMode.Revealed -> {
                state.currentParticipant = null
                state.seed = Random.nextInt()
                state.mode = RaffleMode.Cloud
            }
        }
        return true
    }

    @Composable
    override fun BoxScope.Content() {}

    override fun overlay(state: PresentationState): List<CupAdditionalOverlay> = emptyList()
}

/**
 * DSL helper that registers the [RafflePlugin] inside a CuP configuration block.
 *
 * ```kotlin
 * Presentation(configuration = {
 *     raffle(raffleState, RaffleData.participants)
 * })
 * ```
 */
@CupConfigurationDsl
fun CupConfigurationBuilder.raffle(state: RaffleState, participants: List<String>) {
    plugin(RafflePlugin(state, participants))
}
