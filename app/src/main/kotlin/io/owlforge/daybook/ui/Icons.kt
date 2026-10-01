package io.owlforge.daybook.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun PencilIcon(color: Color, size: Dp = 22.dp) {
    Canvas(Modifier.size(size)) {
        val s = this.size.minDimension
        val tip = Path().apply {
            moveTo(s * 0.10f, s * 0.90f)
            lineTo(s * 0.20f, s * 0.60f)
            lineTo(s * 0.40f, s * 0.80f)
            close()
        }
        drawPath(tip, color)
        drawLine(
            color, Offset(s * 0.32f, s * 0.68f), Offset(s * 0.76f, s * 0.24f),
            strokeWidth = s * 0.22f, cap = StrokeCap.Round
        )
    }
}

@Composable
fun ClockIcon(color: Color, size: Dp = 18.dp) {
    Canvas(Modifier.size(size)) {
        val s = this.size.minDimension
        val sw = s * 0.12f
        val c = Offset(s / 2, s / 2)
        drawCircle(color, radius = s / 2 - sw / 2, style = Stroke(sw))
        drawLine(color, c, Offset(s / 2, s * 0.26f), strokeWidth = sw, cap = StrokeCap.Round)
        drawLine(color, c, Offset(s * 0.68f, s * 0.58f), strokeWidth = sw, cap = StrokeCap.Round)
    }
}
