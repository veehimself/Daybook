package io.owlforge.daybook.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.owlforge.daybook.data.PlanTask
import io.owlforge.daybook.data.TaskStatus
import io.owlforge.daybook.ui.Ink
import io.owlforge.daybook.ui.MainViewModel
import io.owlforge.daybook.ui.PulseDot
import io.owlforge.daybook.ui.Segmented
import io.owlforge.daybook.ui.StatusBadge
import io.owlforge.daybook.ui.Txt
import io.owlforge.daybook.ui.bounceClick
import io.owlforge.daybook.ui.entrance
import io.owlforge.daybook.util.fmtCountdown
import io.owlforge.daybook.util.fmtTime
import io.owlforge.daybook.util.greeting
import kotlinx.coroutines.delay
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox

@Composable
fun HomeScreen(vm: MainViewModel, name: String) {
    val tasks by vm.tasks.collectAsStateWithLifecycle()
    val offset by vm.dayOffset.collectAsStateWithLifecycle()

    // 1s ticks only while a task is running (drives the in-app countdown), otherwise relaxed.
    val now by produceState(System.currentTimeMillis(), tasks) {
        while (true) {
            value = System.currentTimeMillis()
            val live = tasks.any { value >= it.startMillis && value < it.endMillis }
            delay(if (live) 1_000L else 15_000L)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(20.dp))
        Txt(greeting(), size = 14.sp, color = Ink.muted, modifier = Modifier.entrance(0))
        Txt(name, size = 34.sp, weight = FontWeight.Bold, modifier = Modifier.entrance(1))
        Spacer(Modifier.height(18.dp))
        Segmented(listOf("Today", "Tomorrow"), offset) { vm.dayOffset.value = it }
        SummaryStrip(tasks)

        LazyColumn(
            contentPadding = PaddingValues(bottom = 190.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (tasks.isEmpty()) item(key = "empty") { EmptyState(offset) }
            itemsIndexed(tasks, key = { _, t -> t.id }) { i, t ->
                TaskCard(
                    t = t,
                    now = now,
                    modifier = Modifier.animateItem().entrance(i),
                    onClick = { vm.openTaskId.value = t.id },
                    onDelete = { vm.delete(t) },
                )
            }
        }
    }
}

@Composable
private fun SummaryStrip(tasks: List<PlanTask>) {
    val done = tasks.count { it.status == TaskStatus.COMPLETED }
    val frac by animateFloatAsState(
        if (tasks.isEmpty()) 0f else done / tasks.size.toFloat(),
        spring(stiffness = Spring.StiffnessLow), label = "summary"
    )
    Column(Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Txt("${tasks.size} ${if (tasks.size == 1) "task" else "tasks"}", size = 13.sp, color = Ink.muted)
            Spacer(Modifier.width(8.dp))
            Txt("·", size = 13.sp, color = Ink.muted)
            Spacer(Modifier.width(8.dp))
            Txt("$done done", size = 13.sp, color = Ink.mint, weight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(Ink.line)) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(frac)
                    .background(Brush.horizontalGradient(listOf(Ink.violet, Ink.mint)), CircleShape)
            )
        }
    }
}

@Composable
private fun EmptyState(offset: Int) {
    val bob by rememberInfiniteTransition(label = "bob").animateFloat(
        -20f, 20f, infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "b"
    )
    Column(
        Modifier.fillMaxWidth().padding(top = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Txt(
            if (offset == 1) "🌙" else "☀️", size = 64.sp,
            modifier = Modifier.graphicsLayer { translationY = bob }
        )
        Spacer(Modifier.height(14.dp))
        Txt(
            if (offset == 1) "Tomorrow is a blank page" else "Nothing planned today",
            size = 18.sp, weight = FontWeight.SemiBold, align = TextAlign.Center
        )
        Spacer(Modifier.height(4.dp))
        Txt("Tap “Plan a task” to begin", size = 14.sp, color = Ink.muted, align = TextAlign.Center)
    }
}

@Composable
private fun TaskCard(
    t: PlanTask,
    now: Long,
    modifier: Modifier,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    val pending = t.status == TaskStatus.PENDING
    val active = pending && now >= t.startMillis && now < t.endMillis
    val needsReview = pending && now >= t.endMillis
    val accent = when {
        t.status == TaskStatus.COMPLETED -> Ink.mint
        t.status == TaskStatus.INCOMPLETE -> Ink.coral
        active -> Ink.violet
        needsReview -> Ink.amber
        else -> Ink.muted
    }
    val shape = RoundedCornerShape(24.dp)
    val delete = SwipeAction(
        onSwipe = onDelete,
        icon = { Txt("Delete", weight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 24.dp)) },
        background = Ink.coral,
    )

    SwipeableActionsBox(modifier = modifier.clip(shape), endActions = listOf(delete)) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(Ink.card)
                .border(1.dp, if (active) Ink.violet.copy(alpha = 0.8f) else Ink.line, shape)
                .bounceClick(onClick)
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (active) { PulseDot(); Spacer(Modifier.width(8.dp)) }
                    Txt(
                        "${fmtTime(t.startMillis)} – ${fmtTime(t.endMillis)}",
                        size = 12.sp, color = accent, weight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.height(4.dp))
                Txt(t.title, size = 18.sp, weight = FontWeight.SemiBold, maxLines = 2)
                if (needsReview) {
                    Spacer(Modifier.height(10.dp))
                    Box(Modifier.border(1.5.dp, Ink.amber, CircleShape).padding(horizontal = 12.dp, vertical = 4.dp)) {
                        Txt("Tap to review", size = 12.sp, color = Ink.amber, weight = FontWeight.SemiBold)
                    }
                }
            }
            when {
                active -> CountdownRing(t, now)
                else -> StatusBadge(t.status)
            }
        }
    }
}

/** In-app persistent countdown, mirrors the live notification. */
@Composable
private fun CountdownRing(t: PlanTask, now: Long) {
    val total = (t.endMillis - t.startMillis).toFloat()
    val left = (t.endMillis - now).coerceAtLeast(0)
    val frac by animateFloatAsState(left / total, tween(900), label = "ring")
    Box(Modifier.size(80.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val sw = 6.dp.toPx()
            val tl = Offset(sw / 2, sw / 2)
            val sz = Size(size.width - sw, size.height - sw)
            drawArc(Ink.line, 0f, 360f, false, tl, sz, style = Stroke(sw))
            drawArc(Ink.violet, -90f, 360f * frac, false, tl, sz, style = Stroke(sw, cap = StrokeCap.Round))
        }
        Txt(fmtCountdown(left), size = 14.sp, weight = FontWeight.Bold, mono = true, align = TextAlign.Center)
    }
}
