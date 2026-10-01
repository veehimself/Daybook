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
import io.owlforge.daybook.ui.TimeWheel
import io.owlforge.daybook.ui.Txt
import io.owlforge.daybook.ui.bounceClick
import io.owlforge.daybook.util.fmtDate
import io.owlforge.daybook.util.fmtDuration
import io.owlforge.daybook.util.fmtMinutes
import io.owlforge.daybook.util.fmtTime
import io.owlforge.daybook.util.zone
import java.time.Instant
import java.time.LocalTime
import kotlin.math.roundToInt

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
    var end by remember { mutableIntStateOf((def + 60).coerceAtMost(23 * 60 + 59)) }
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
        textStyle = TextStyle(color = Ink.text, fontSize = 18.sp, fontWeight = FontWeight.Medium),
        cursorBrush = SolidColor(Ink.violet),
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
        val cur = if (active == 0) start else end
        TimeWheel(hour = cur / 60, minute = cur % 60) { h, m ->
            if (active == 0) start = h * 60 + m else end = h * 60 + m
            error = null
        }
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

    Capsule("Add task", Ink.violet, Modifier.fillMaxWidth(), filled = true) {
        onSubmit(title, start, end) { err ->
            if (err != null) { error = err; tick++ }
        }
    }
}

@Composable
private fun TimeChip(label: String, value: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val c = if (selected) Ink.violet else Ink.line
    Box(
        modifier
            .bounceClick(onClick)
            .background(if (selected) Ink.violet.copy(alpha = 0.16f) else Color.Transparent, RoundedCornerShape(50))
            .border(1.5.dp, c, CircleShape)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Txt(label, size = 13.sp, color = Ink.muted)
            Spacer(Modifier.width(8.dp))
            Txt(value, size = 16.sp, weight = FontWeight.Bold, mono = true)
        }
    }
}

/** Opened from the "did you finish?" notification (or by tapping any task card). */
@Composable
fun ColumnScope.TaskDetailContent(t: PlanTask, onComplete: () -> Unit, onIncomplete: () -> Unit) {
    val date = Instant.ofEpochMilli(t.startMillis).atZone(zone).toLocalDate()
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
    Spacer(Modifier.height(28.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Capsule("Complete", Ink.mint, Modifier.weight(1f), onClick = onComplete)
        Capsule("Incomplete", Ink.coral, Modifier.weight(1f), onClick = onIncomplete)
    }
}
