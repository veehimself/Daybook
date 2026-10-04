package io.owlforge.daybook.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.owlforge.daybook.R
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Right-to-left swipe to delete. The content can never be dragged to the right:
 * its offset is clamped to [-width, 0]. When [enabled] is false the card doesn't move at all,
 * and if it becomes disabled mid-swipe it springs back.
 */
@Composable
fun SwipeToDelete(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onDelete: () -> Unit,
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val offset = remember { Animatable(0f) }
    var width by remember { mutableFloatStateOf(1f) }

    // e.g. the task just started or got marked while the card was being dragged
    LaunchedEffect(enabled) {
        if (!enabled) offset.animateTo(0f, spring(dampingRatio = 0.6f, stiffness = 400f))
    }

    Box(modifier.onSizeChanged { width = it.width.toFloat().coerceAtLeast(1f) }) {
        // revealed layer (invisible while the card is at rest, so no edge bleed)
        Box(
            Modifier
                .matchParentSize()
                .graphicsLayer { alpha = if (offset.value < -1f) 1f else 0f }
                .background(Ink.coral),
            contentAlignment = Alignment.CenterEnd
        ) {
            Row(
                Modifier
                    .padding(end = 24.dp)
                    .graphicsLayer {
                        val p = (-offset.value / (width * 0.4f)).coerceIn(0f, 1f)
                        alpha = p
                        scaleX = 0.8f + 0.2f * p
                        scaleY = 0.8f + 0.2f * p
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppIcon(R.drawable.ic_delete, Ink.text, 20.dp)
                Spacer(Modifier.width(8.dp))
                Txt("Delete", weight = FontWeight.SemiBold, size = 14.sp)
            }
        }

        Box(
            Modifier
                .offset { IntOffset(offset.value.roundToInt(), 0) }
                .draggable(
                    enabled = enabled,
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        scope.launch { offset.snapTo((offset.value + delta).coerceIn(-width, 0f)) }
                    },
                    onDragStopped = { velocity ->
                        if (offset.value < -width * 0.4f || velocity < -1800f) {
                            offset.animateTo(-width, tween(180))
                            onDelete()
                        } else {
                            offset.animateTo(0f, spring(dampingRatio = 0.6f, stiffness = 400f))
                        }
                    }
                )
        ) { content() }
    }
}
