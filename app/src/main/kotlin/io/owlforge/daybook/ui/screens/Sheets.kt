package io.owlforge.daybook.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.owlforge.daybook.data.PlanTask
import io.owlforge.daybook.ui.Capsule
import io.owlforge.daybook.ui.Ink
import io.owlforge.daybook.ui.StatusBadge
import io.owlforge.daybook.ui.TimeWheelValue
import io.owlforge.daybook.ui.Txt
import io.owlforge.daybook.ui.bounceClick
import io.owlforge.daybook.ui.Fonts
import io.owlforge.daybook.util.fmtDate
import io.owlforge.daybook.util.fmtDuration
import io.owlforge.daybook.util.fmtMinutes
import io.owlforge.daybook.util.fmtTime
import io.owlforge.daybook.util.zone
import java.time.Instant
import java.time.LocalTime
import kotlin.math.roundToInt
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.produceState
import kotlinx.coroutines.delay


private fun defaultStartMinutes(dayOffset: Int): Int {
    if (dayOffset == 1) return 9 * 60
    val now = LocalTime.now()
    val rounded = ((now.hour * 60 + now.minute) / 15 + 2) * 15 // next quarter-hour, at least 15 min out
    return rounded.coerceAtMost(22 * 60)
}

@Composable
fun ColumnScope.AddTaskContent(
    dayOffset: Int,
    onSubmit: (title: String, startMin: Int, endMin: Int, onResult: (String?) -> Unit) -> Unit,
) {
    val def = remember { defaultStartMinutes(dayOffset) }
    var title by remember { mutableStateOf("") }
    var start by remember { mutableIntStateOf(def) }

    // Quick tasks: end defaults to start + 30 min until the user sets it by hand.
    fun quickEnd(startMin: Int) = (startMin + 30).coerceAtMost(23 * 60 + 59)
    var end by remember { mutableIntStateOf(quickEnd(def)) }
    var endTouched by remember { mutableStateOf(false) }

    var active by remember { mutableIntStateOf(0) }
    var error by remember { mutableStateOf<String?>(null) }
    var tick by remember { mutableIntStateOf(0) }
    val shake = remember { Animatable(0f) }
    LaunchedEffect(tick) {
        if (tick > 0) for (v in listOf(-14f, 14f, -9f, 9f, 0f)) shake.animateTo(v, tween(45))
    }

    Txt(if (dayOffset == 1) "Plan tomorrow" else "Add to today", size = 26.sp, weight = FontWeight.Bold)
    Spacer(Modifier.height(16.dp))

    BasicTextField(
        value = title,
        onValueChange = { title = it; error = null },
        singleLine = true,
        textStyle = TextStyle(
            color = Ink.text, fontSize = 18.sp, fontWeight = FontWeight.Medium, fontFamily = Fonts.sans
        ),
        cursorBrush = SolidColor(Ink.accent),
        decorationBox = { inner ->
            Box {
                if (title.isEmpty()) Txt("What's the task?", size = 18.sp, color = Ink.muted)
                inner()
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Ink.line, RoundedCornerShape(18.dp))
            .padding(16.dp)
    )
    Spacer(Modifier.height(16.dp))

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        TimeChip("Starts", fmtMinutes(start), active == 0, Modifier.weight(1f)) { active = 0 }
        TimeChip("Ends", fmtMinutes(end), active == 1, Modifier.weight(1f)) { active = 1 }
    }
    Spacer(Modifier.height(8.dp))

    key(active) {
        TimeWheelValue(if (active == 0) start else end) { v ->
            error = null
            if (active == 0) {
                start = v
                // follow the start with +30 min (animated by the chip + wheel), unless the user took over
                if (!endTouched || end <= v) end = quickEnd(v)
            } else {
                endTouched = true
                end = v // the hour wheel flips AM/PM itself when it crosses 11 ↔ 12
            }        
        }
    }

    if (end > start) {
        Txt(
            "Duration · ${fmtDuration((end - start) * 60_000L)}",
            size = 13.sp, color = Ink.muted,
            modifier = Modifier.padding(top = 8.dp)
        )
    }

    AnimatedVisibility(visible = error != null) {
        Txt(
            error ?: "", color = Ink.coral, size = 14.sp, weight = FontWeight.Medium,
            modifier = Modifier
                .padding(top = 8.dp)
                .offset { IntOffset(shake.value.roundToInt(), 0) }
        )
    }
    Spacer(Modifier.height(16.dp))

    Capsule("Add task", Ink.accent, Modifier.fillMaxWidth(), filled = true) {
        onSubmit(title, start, end) { err ->
            if (err != null) { error = err; tick++ }
        }
    }
}

@Composable
private fun TimeChip(label: String, value: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val c = if (selected) Ink.accent else Ink.line
    Box(
        modifier
            .bounceClick(onClick)
            .background(if (selected) Ink.accent.copy(alpha = 0.14f) else Color.Transparent, RoundedCornerShape(50))
            .border(1.5.dp, c, CircleShape)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Txt(label, size = 13.sp, color = Ink.muted)
            Spacer(Modifier.width(8.dp))
            AnimatedContent(
                targetState = value,
                transitionSpec = {
                    (slideInVertically(tween(280)) { it } + fadeIn(tween(280))) togetherWith
                        (slideOutVertically(tween(200)) { -it } + fadeOut(tween(160)))
                },
                label = "chipValue"
            ) { v -> Txt(v, size = 16.sp, weight = FontWeight.Bold, mono = true) }
        }
    }
}

/** Opened from the "did you finish?" notification (or by tapping any task card). */
@Composable
fun ColumnScope.TaskDetailContent(t: PlanTask, onComplete: () -> Unit, onIncomplete: () -> Unit) {
    val date = Instant.ofEpochMilli(t.startMillis).atZone(zone).toLocalDate()
    val endDate = Instant.ofEpochMilli(t.endMillis).atZone(zone).toLocalDate()

    // Wall-clock gate (date + time): ticks only until the end passes, then unlocks live.
    val now by produceState(System.currentTimeMillis(), t.endMillis) {
        while (value < t.endMillis) {
            delay(1_000)
            value = System.currentTimeMillis()
        }
    }
    val canMark = now >= t.endMillis

    Txt(fmtDate(date), size = 14.sp, color = Ink.muted)
    Spacer(Modifier.height(6.dp))
    Txt(t.title, size = 28.sp, weight = FontWeight.Bold)
    Spacer(Modifier.height(16.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.border(1.dp, Ink.line, CircleShape).padding(horizontal = 14.dp, vertical = 6.dp)) {
            Txt("${fmtTime(t.startMillis)} – ${fmtTime(t.endMillis)}", size = 14.sp, mono = true)
        }
        Spacer(Modifier.width(10.dp))
        Box(Modifier.border(1.dp, Ink.line, CircleShape).padding(horizontal = 14.dp, vertical = 6.dp)) {
            Txt(fmtDuration(t.endMillis - t.startMillis), size = 14.sp, color = Ink.muted)
        }
        Spacer(Modifier.weight(1f))
        StatusBadge(t.status)
    }
    Spacer(Modifier.height(24.dp))
    if (!canMark) {
        Txt(
            "You can mark this once it ends · ${fmtDate(endDate)}, ${fmtTime(t.endMillis)}",
            size = 13.sp, color = Ink.muted,
            modifier = Modifier.padding(bottom = 12.dp)
        )
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Capsule("Complete", Ink.mint, Modifier.weight(1f), enabled = canMark, onClick = onComplete)
        Capsule("Incomplete", Ink.coral, Modifier.weight(1f), enabled = canMark, onClick = onIncomplete)
    }
}
