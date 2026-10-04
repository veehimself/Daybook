package io.owlforge.daybook.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Warm gradient sun with a soft glow and slowly rotating rays. */
@Composable
fun SunIllustration(diameter: Dp = 96.dp, modifier: Modifier = Modifier) {
    val spin by rememberInfiniteTransition(label = "sun").animateFloat(
        0f, 360f, infiniteRepeatable(tween(60_000, easing = LinearEasing)), label = "spin"
    )
    Canvas(modifier.size(diameter)) {
        val r = this.size.minDimension / 2
        val c = center

        // glow
        drawCircle(
            Brush.radialGradient(
                listOf(Color(0xFFFFC857).copy(alpha = 0.40f), Color.Transparent),
                center = c, radius = r
            ),
            radius = r, center = c
        )

        // rays: alternating long / short
        rotate(spin, pivot = c) {
            repeat(12) { i ->
                val a = (i * 2 * PI / 12).toFloat()
                val dir = Offset(cos(a), sin(a))
                val to = r * (if (i % 2 == 0) 0.88f else 0.76f)
                drawLine(
                    Color(0xFFFFB830),
                    c + dir * (r * 0.58f),
                    c + dir * to,
                    strokeWidth = r * 0.085f,
                    cap = StrokeCap.Round
                )
            }
        }

        // core
        val core = r * 0.38f
        drawCircle(
            Brush.radialGradient(
                listOf(Color(0xFFFFF0B3), Color(0xFFFFC23D), Color(0xFFFF8A3D)),
                center = c - Offset(core * 0.25f, core * 0.25f),
                radius = core * 1.5f
            ),
            radius = core, center = c
        )
    }
}

/** Gradient crescent with a soft halo and twinkling stars. */
@Composable
fun MoonIllustration(diameter: Dp = 96.dp, modifier: Modifier = Modifier) {
    val twinkle by rememberInfiniteTransition(label = "moon").animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "twinkle"
    )
    Canvas(modifier.size(diameter)) {
        val r = this.size.minDimension / 2
        val c = center

        // halo
        drawCircle(
            Brush.radialGradient(
                listOf(Color(0xFF9DB4FF).copy(alpha = 0.28f), Color.Transparent),
                center = c, radius = r
            ),
            radius = r, center = c
        )

        // crescent = full disc minus an offset disc
        val moonR = r * 0.52f
        val moonC = c + Offset(-r * 0.06f, r * 0.02f)
        val cut = Path().apply {
            addOval(Rect(center = moonC + Offset(moonR * 0.42f, -moonR * 0.26f), radius = moonR))
        }
        clipPath(cut, clipOp = ClipOp.Difference) {
            drawCircle(
                Brush.linearGradient(
                    listOf(Color(0xFFFFF8E1), Color(0xFFD9D2B0)),
                    start = moonC - Offset(moonR, moonR),
                    end = moonC + Offset(moonR, moonR)
                ),
                radius = moonR, center = moonC
            )
        }

        // stars
        fun star(p: Offset, rad: Float, phase: Float) =
            drawCircle(Color.White.copy(alpha = 0.35f + 0.65f * phase), radius = rad, center = p)
        star(c + Offset(r * 0.55f, -r * 0.55f), r * 0.045f, twinkle)
        star(c + Offset(r * 0.78f, -r * 0.10f), r * 0.030f, 1f - twinkle)
        star(c + Offset(r * 0.45f, r * 0.50f), r * 0.035f, twinkle)
    }
}
