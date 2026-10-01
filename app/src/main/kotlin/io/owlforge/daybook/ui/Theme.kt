package io.owlforge.daybook.ui

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import io.owlforge.daybook.R
import androidx.compose.ui.graphics.Color

/** Custom palette — no Material theme anywhere. */
object Ink {
    val bg = Color(0xFF0A0A12)
    val surface = Color(0xFF14152A)
    val card = Color(0xFF1A1C36)
    val line = Color(0xFF2B2E52)
    val text = Color(0xFFF4F4FB)
    val muted = Color(0xFF8E91B8)
    val accent = Color(0xFFEDE6D6)
    val mint = Color(0xFF3DFFB5)
    val coral = Color(0xFFFF5C7A)
    val amber = Color(0xFFFFC857)
}

@OptIn(ExperimentalTextApi::class)
object Fonts {
    private val weights = listOf(
        FontWeight.Light, FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold, FontWeight.Bold
    )

    val sans = FontFamily(
        weights.map { w ->
            Font(R.font.space_grotesk, w, variationSettings = FontVariation.Settings(FontVariation.weight(w.weight)))
        }
    )

    val mono = FontFamily(
        weights.map { w ->
            Font(R.font.jetbrains_mono, w, variationSettings = FontVariation.Settings(FontVariation.weight(w.weight)))
        }
    )
}
