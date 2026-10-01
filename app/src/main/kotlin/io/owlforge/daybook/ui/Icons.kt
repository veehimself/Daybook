package io.owlforge.daybook.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.owlforge.daybook.R

/** Tinted vector drawable. */
@Composable
fun AppIcon(res: Int, color: Color, size: Dp, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(res),
        contentDescription = null,
        modifier = modifier.size(size),
        colorFilter = ColorFilter.tint(color),
    )
}

@Composable
fun PencilIcon(color: Color, size: Dp = 22.dp) = AppIcon(R.drawable.ic_edit, color, size)

@Composable
fun ClockIcon(color: Color, size: Dp = 18.dp) = AppIcon(R.drawable.ic_schedule, color, size)
