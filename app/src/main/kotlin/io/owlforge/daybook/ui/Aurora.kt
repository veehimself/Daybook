package io.owlforge.daybook.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import kotlinx.coroutines.delay
import java.time.LocalTime
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

private class Sky(val hour: Float, val a: Color, val b: Color, val c: Color)

private val skies = listOf(
    Sky(0f,    Color(0xFF0B1B34), Color(0xFF16305A), Color(0xFF0A3140)), // midnight
    Sky(5.5f,  Color(0xFFD9694B), Color(0xFFE9B872), Color(0xFF34496E)), // dawn
    Sky(9f,    Color(0xFFF2A65A), Color(0xFF6FB3A8), Color(0xFFF6D88A)), // morning
    Sky(13.5f, Color(0xFF3FA0D8), Color(0xFF4FD1C5), Color(0xFF9AD9F0)), // afternoon
    Sky(18f,   Color(0xFFFF6F4E), Color(0xFFE0457B), Color(0xFF5B4B8A)), // sunset
    Sky(21f,   Color(0xFF1B3A6B), Color(0xFF24507F), Color(0xFF0E4A4F)), // night
    Sky(24f,   Color(0xFF0B1B34), Color(0xFF16305A), Color(0xFF0A3140)), // back to midnight
)

private fun nowHour(): Float = LocalTime.now().let { it.hour + it.minute / 60f }

private fun skyAt(hour: Float): Triple<Color, Color, Color> {
    val i = skies.indexOfLast { it.hour <= hour }.coerceAtMost(skies.lastIndex - 1)
    val from = skies[i]
    val to = skies[i + 1]
    val t = ((hour - from.hour) / (to.hour - from.hour)).coerceIn(0f, 1f)
    val e = t * t * (3 - 2 * t) // smoothstep, so palettes melt into each other
    return Triple(lerp(from.a, to.a, e), lerp(from.b, to.b, e), lerp(from.c, to.c, e))
}

@Composable
fun SkyBackground() {
    val hour by produceState(nowHour()) {
        while (true) { delay(30_000); value = nowHour() }
    }
    val (c1, c2, c3) = skyAt(hour)

    // sin/cos of whole multiples of t, so the 60s loop restarts seamlessly
    val t by rememberInfiniteTransition(label = "sky").animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(60_000, easing = LinearEasing)),
        label = "t"
    )

    Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val r = max(w, h) * 0.75f
        val tt = t // read in the draw phase: animates without recomposing

        fun blob(color: Color, cx: Float, cy: Float, alpha: Float) {
            val center = Offset(cx, cy)
            drawCircle(
                Brush.radialGradient(
                    0f to color.copy(alpha = alpha),
                    0.55f to color.copy(alpha = alpha * 0.4f),
                    1f to Color.Transparent,
                    center = center, radius = r
                ),
                radius = r, center = center
            )
        }
        blob(c1, w * (0.20f + 0.15f * sin(tt)), h * (0.15f + 0.08f * cos(tt)), 0.55f)
        blob(c2, w * (0.85f + 0.12f * cos(tt)), h * (0.45f + 0.10f * sin(2 * tt)), 0.45f)
        blob(c3, w * (0.35f + 0.20f * sin(2 * tt)), h * (0.95f + 0.05f * cos(tt)), 0.50f)

        // keeps text readable on the brighter daytime palettes
        drawRect(Brush.verticalGradient(listOf(Ink.bg.copy(alpha = 0.20f), Ink.bg.copy(alpha = 0.55f))))
    }
}
