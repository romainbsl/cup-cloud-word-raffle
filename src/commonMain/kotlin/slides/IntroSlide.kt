package slides

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.kodein.cup.SLIDE_SIZE_16_9
import net.kodein.cup.Slide
import net.kodein.cup.SlideSpecs
import net.kodein.theme.KodeinColors
import net.kodein.theme.compose.Color
import net.kodein.theme.compose.KodeinVectorImages
import net.kodein.theme.compose.img.KodeinMonogram
import net.kodein.theme.cup.kStyled
import net.kodein.theme.cup.ui.KodeinLogo

val introSlide by Slide(
    specs = SlideSpecs(size = SLIDE_SIZE_16_9)
) { _ ->
    KodeinLogo(
        division = "Koders",
    ) {
        Text("KotlinConf Conference Pass Giveaway")
    }
    Spacer(Modifier.height(16.dp))
    Text(
        text = "2 jours à la KotlinConf 2026",
        style = MaterialTheme.typography.subtitle1,
        color = MaterialTheme.colors.onBackground,
    )
    Text(
        text = "à Munich les 21 & 22 mai.",
        style = MaterialTheme.typography.subtitle1,
        color = MaterialTheme.colors.onBackground,
    )
    Spacer(Modifier.height(8.dp))
    Text(
        text = "Le déplacement et l'hébergement ne sont pas pris en charge.",
        style = MaterialTheme.typography.subtitle2,
        color = MaterialTheme.colors.onBackground.copy(alpha = 0.5f),
    )
}
