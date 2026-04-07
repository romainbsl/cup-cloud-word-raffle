package slides

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.kodein.cup.SLIDE_SIZE_16_9
import net.kodein.cup.Slide
import net.kodein.cup.SlideSpecs
import net.kodein.theme.cup.ui.KodeinLogo

val outroSlide by Slide(
    specs = SlideSpecs(size = SLIDE_SIZE_16_9)
) { _ ->
    KodeinLogo(
        division = "Koders",
    ) {
        Text("")
    }
    Spacer(Modifier.height(16.dp))
    Text(
        text = "Merci !",
        style = MaterialTheme.typography.subtitle1,
        color = MaterialTheme.colors.onBackground,
    )
}
