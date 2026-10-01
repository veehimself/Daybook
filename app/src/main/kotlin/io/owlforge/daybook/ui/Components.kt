package io.owlforge.daybook.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.owlforge.daybook.data.TaskStatus
import kotlinx.coroutines.delay

// ───────────────────────── text ─────────────────────────

@Composable
fun Txt(
    text: String,
    modifier: Modifier = Modifier,
    size: TextUnit = 15.sp,
    weight: FontWeight = FontWeight.Normal,
    color: Color = Ink.text,
    mono: Boolean = false,
    align: TextAlign = TextAlign.Start,
    maxLines: Int = Int.MAX_VALUE,
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = TextStyle(
            color = color,
            fontSize = size,
            fontWeight = weight,
            fontFamily = if (mono) FontFamily.Monospace else FontFamily.SansSerif,
            textAlign = align,
        ),
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
    )
}

// ───────────────────────── motion helpers ─────────────────────────

/** Springy press-down scale + click. */
fun Modifier.bounceClick(onClick: () -> Unit): Modifier = composed {
    val src = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    val s by animateFloatAsState(
        if (pressed) 0.94f else 1f,
        spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMedium),
        label = "press"
    )
    graphicsLayer { scaleX = s; scaleY = s }
        .clickable(interactionSource = src, indication = null, onClick = onClick)
}

/** Staggered slide-up + fade-in on first composition. */
@Composable
fun Modifier.entrance(index: Int = 0): Modifier {
    val a = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(minOf(index, 8) * 55L)
        a.animateTo(1f, spring(dampingRatio = 0.75f, stiffness = 220f))
    }
    return graphicsLayer {
        alpha = a.value.coerceIn(0f, 1f)
        translationY = (1f - a.value) * 70f
    }
}

@Composable
fun AuroraBackground() {
    val t = rememberInfiniteTransition(label = "aurora")
    val a by t.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(16000, easing = LinearEasing), RepeatMode.Reverse),
        label = "a"
    )
    Canvas(Modifier.fillMaxSize()) {
        val r = size.width * 0.95f
        val c1 = Offset(size.width * (0.1f + 0.7f * a), size.height * (0.12f + 0.06f * a))
        drawCircle(
            Brush.radialGradient(listOf(Ink.violet.copy(alpha = 0.38f), Color.Transparent), center = c1, radius = r),
            radius = r, center = c1
        )
        val c2 = Offset(size.width * (0.9f - 0.7f * a), size.height * (0.85f - 0.1f * a))
        drawCircle(
            Brush.radialGradient(listOf(Ink.mint.copy(alpha = 0.16f), Color.Transparent), center = c2, radius = r),
            radius = r, center = c2
        )
    }
}

@Composable
fun PulseDot(color: Color = Ink.mint) {
    val p by rememberInfiniteTransition(label = "dot").animateFloat(
        0.3f, 1f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "p"
    )
    Box(
        Modifier
            .size(8.dp)
            .graphicsLayer { alpha = p; scaleX = 0.8f + p * 0.4f; scaleY = 0.8f + p * 0.4f }
            .background(color, CircleShape)
    )
}

// ───────────────────────── buttons & badges ─────────────────────────

@Composable
fun Capsule(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    filled: Boolean = false,
    onClick: () -> Unit,
) {
    Box(
        modifier
            .bounceClick(onClick)
            .background(if (filled) color else Color.Transparent, CircleShape)
            .border(1.5.dp, color, CircleShape)
            .padding(horizontal = 22.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Txt(label, weight = FontWeight.SemiBold, color = if (filled) Ink.bg else color, size = 16.sp)
    }
}

/** ✔ / ✕ inside a capsule outline. */
@Composable
fun StatusBadge(status: Int, showPending: Boolean = false) {
    val (sym, c) = when (status) {
        TaskStatus.COMPLETED -> "✔" to Ink.mint
        TaskStatus.INCOMPLETE -> "✕" to Ink.coral
        else -> if (showPending) "–" to Ink.muted else return
    }
    Box(
        Modifier
            .border(1.5.dp, c, CircleShape)
            .padding(horizontal = 14.dp, vertical = 4.dp)
    ) { Txt(sym, color = c, weight = FontWeight.Bold, size = 14.sp) }
}

@Composable
fun Segmented(options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    val w = 112.dp
    val x by animateDpAsState(w * selected, spring(dampingRatio = 0.7f, stiffness = 400f), label = "seg")
    Box(
        Modifier
            .clip(CircleShape)
            .border(1.dp, Ink.line, CircleShape)
            .padding(4.dp)
    ) {
        Box(
            Modifier
                .offset(x = x)
                .width(w)
                .height(40.dp)
                .background(Ink.violet, CircleShape)
        )
        Row {
            options.forEachIndexed { i, s ->
                Box(
                    Modifier
                        .width(w)
                        .height(40.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onSelect(i) },
                    contentAlignment = Alignment.Center
                ) {
                    Txt(s, weight = FontWeight.SemiBold, color = if (i == selected) Ink.text else Ink.muted)
                }
            }
        }
    }
}

// ───────────────────────── bottom sheet ─────────────────────────

@Composable
fun BottomSheet(visible: Boolean, onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    BackHandler(enabled = visible, onBack = onDismiss)
    val shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    Box(Modifier.fillMaxSize()) {
        AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss
                    )
            )
        }
        AnimatedVisibility(
            visible = visible,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(spring(dampingRatio = 0.8f, stiffness = 300f)) { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(Ink.surface)
                    .border(1.dp, Ink.line, shape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* swallow taps so the scrim doesn't get them */ }
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(24.dp),
                content = content
            )
        }
    }
}

// ───────────────────────── time wheels ─────────────────────────

@Composable
fun Wheel(items: List<String>, initial: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val itemH = 46.dp
    val itemPx = with(LocalDensity.current) { itemH.toPx() }
    val state = rememberLazyListState(initialFirstVisibleItemIndex = initial)
    val fling = rememberSnapFlingBehavior(state)
    val selected by remember {
        derivedStateOf {
            (state.firstVisibleItemIndex + if (state.firstVisibleItemScrollOffset > itemPx / 2) 1 else 0)
                .coerceIn(0, items.lastIndex)
        }
    }
    LaunchedEffect(selected) { onSelect(selected) }

    Box(modifier.height(itemH * 3), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(itemH)
                .background(Ink.violet.copy(alpha = 0.16f), RoundedCornerShape(16.dp))
        )
        LazyColumn(
            state = state,
            flingBehavior = fling,
            contentPadding = PaddingValues(vertical = itemH),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(items) { i, s ->
                Box(Modifier.height(itemH).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    val on = i == selected
                    Txt(
                        s,
                        size = if (on) 24.sp else 19.sp,
                        weight = if (on) FontWeight.Bold else FontWeight.Normal,
                        color = if (on) Ink.text else Ink.muted
                    )
                }
            }
        }
    }
}

private val hours12 = (1..12).map { it.toString() }
private val minutes60 = (0..59).map { "%02d".format(it) }
private val ampm = listOf("AM", "PM")

/** [hour] is 0–23. [onChange] receives 24h hour + minute. */
@Composable
fun TimeWheel(hour: Int, minute: Int, onChange: (Int, Int) -> Unit) {
    var h12 by remember { mutableIntStateOf(if (hour % 12 == 0) 12 else hour % 12) }
    var m by remember { mutableIntStateOf(minute) }
    var pm by remember { mutableStateOf(hour >= 12) }
    fun push() = onChange((h12 % 12) + if (pm) 12 else 0, m)

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Wheel(hours12, h12 - 1, { h12 = it + 1; push() }, Modifier.weight(1f))
        Wheel(minutes60, m, { m = it; push() }, Modifier.weight(1f))
        Wheel(ampm, if (pm) 1 else 0, { pm = it == 1; push() }, Modifier.weight(1f))
    }
}
