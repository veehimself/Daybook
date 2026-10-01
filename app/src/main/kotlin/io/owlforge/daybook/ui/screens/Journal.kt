package io.owlforge.daybook.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.owlforge.daybook.ui.DayReport
import io.owlforge.daybook.ui.Ink
import io.owlforge.daybook.ui.MainViewModel
import io.owlforge.daybook.ui.StatusBadge
import io.owlforge.daybook.ui.Txt
import io.owlforge.daybook.ui.entrance
import io.owlforge.daybook.util.fmtDate
import io.owlforge.daybook.util.fmtTime

@Composable
fun JournalScreen(vm: MainViewModel) {
    val reports by vm.reports.collectAsStateWithLifecycle()
    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(20.dp))
        Txt("Journal", size = 34.sp, weight = FontWeight.Bold, modifier = Modifier.entrance(0))
        Txt("Your days, in numbers", size = 14.sp, color = Ink.muted, modifier = Modifier.entrance(1))
        Spacer(Modifier.height(20.dp))
        if (reports.isEmpty()) {
            Txt("Complete a few tasks and your daily reports will show up here.", color = Ink.muted)
        }
        LazyColumn(
            contentPadding = PaddingValues(bottom = 130.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(reports, key = { _, r -> r.date.toEpochDay() }) { i, r ->
                DayCard(r, Modifier.animateItem().entrance(i))
            }
        }
    }
}

@Composable
private fun DayCard(r: DayReport, modifier: Modifier) {
    val frac by animateFloatAsState(
        if (r.total == 0) 0f else r.done / r.total.toFloat(),
        spring(stiffness = Spring.StiffnessLow), label = "dayfrac"
    )
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier
            .fillMaxWidth()
            .background(Ink.card, shape)
            .border(1.dp, Ink.line, shape)
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Txt(fmtDate(r.date), size = 18.sp, weight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            Txt("${r.done}/${r.total}", size = 24.sp, weight = FontWeight.Bold, mono = true, color = Ink.mint)
        }
        Spacer(Modifier.height(12.dp))
        Box(Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(Ink.line)) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(frac)
                    .background(Brush.horizontalGradient(listOf(Ink.accent, Ink.mint)), CircleShape)
            )
        }
        Spacer(Modifier.height(14.dp))
        r.tasks.forEach { t ->
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                StatusBadge(t.status, showPending = true)
                Spacer(Modifier.width(12.dp))
                Txt(t.title, modifier = Modifier.weight(1f), maxLines = 1, weight = FontWeight.Medium)
                Spacer(Modifier.width(8.dp))
                Txt(fmtTime(t.startMillis), size = 12.sp, color = Ink.muted, mono = true)
            }
        }
    }
}
