package io.owlforge.daybook.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.owlforge.daybook.data.Profile
import io.owlforge.daybook.ui.Avatar
import io.owlforge.daybook.ui.ClockIcon
import io.owlforge.daybook.ui.Ink
import io.owlforge.daybook.ui.MainViewModel
import io.owlforge.daybook.ui.PencilIcon
import io.owlforge.daybook.ui.ProfileSheet
import io.owlforge.daybook.ui.Txt
import io.owlforge.daybook.ui.bounceClick
import io.owlforge.daybook.ui.entrance
import io.owlforge.daybook.util.fmtHm
import androidx.compose.ui.draw.shadow

@Composable
fun ProfileScreen(vm: MainViewModel, p: Profile) {
    val wave by rememberInfiniteTransition(label = "wave").animateFloat(
        -14f, 14f, infiniteRepeatable(tween(650), RepeatMode.Reverse), label = "w"
    )

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 130.dp)
    ) {
        Spacer(Modifier.height(24.dp))

        Row(Modifier.entrance(0), verticalAlignment = Alignment.CenterVertically) {
            Txt("Hey", size = 44.sp, weight = FontWeight.Bold)
            Spacer(Modifier.width(12.dp))
            Txt(
                "👋🏽", size = 40.sp,                
                modifier = Modifier.graphicsLayer {
                    rotationZ = wave
                    transformOrigin = TransformOrigin(0.7f, 0.8f)
                }
            )
        }
        Spacer(Modifier.height(28.dp))

        Row(Modifier.entrance(1), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.bounceClick { vm.profileSheet.value = ProfileSheet.AVATAR }) {
                Avatar(p.avatar, 96.dp)
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .size(30.dp)
                        .background(Ink.accent, CircleShape)
                        .border(2.dp, Ink.bg, CircleShape),
                    contentAlignment = Alignment.Center
                ) { PencilIcon(Ink.bg, 14.dp) }
            }
            Spacer(Modifier.width(20.dp))
            Txt(
                p.name, size = 34.sp, weight = FontWeight.Bold, maxLines = 1,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(Modifier.width(6.dp))
            Box(
                Modifier.size(44.dp).bounceClick { vm.profileSheet.value = ProfileSheet.NAME },
                contentAlignment = Alignment.Center
            ) { PencilIcon(Ink.text, 22.dp) }
        }
        Spacer(Modifier.height(32.dp))

        ReminderCard(p.planHour, p.planMinute, Modifier.entrance(2)) {
            vm.profileSheet.value = ProfileSheet.TIME
        }
    }
}

@Composable
private fun ReminderCard(hour: Int, minute: Int, modifier: Modifier, onClick: () -> Unit) {
    val shape = RoundedCornerShape(32.dp)
    Column(
        modifier
            .fillMaxWidth()
            .bounceClick(onClick) // first, so the shadow and surface scale together on press
            .shadow(24.dp, shape, clip = false, ambientColor = Color.Black, spotColor = Color.Black)
            .background(Ink.card, shape)
            .background(Brush.linearGradient(listOf(Color.White.copy(alpha = 0.08f), Color.Transparent)), shape)
            .padding(24.dp)
    ) {
        // 1 · eyebrow + edit affordance
        Row(verticalAlignment = Alignment.CenterVertically) {
            ClockIcon(Ink.muted, 16.dp)
            Spacer(Modifier.width(8.dp))
            Txt(
                "DAILY REMINDER",
                size = 12.sp, weight = FontWeight.SemiBold, color = Ink.muted, tracking = 1.4.sp
            )
            Spacer(Modifier.weight(1f))
            Box(
                Modifier.size(36.dp).background(Ink.accent.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) { PencilIcon(Ink.accent, 16.dp) }
        }
        Spacer(Modifier.height(20.dp))

        // 2 · hero time: big digits, smaller AM/PM sharing the baseline
        AnimatedContent(
            targetState = hour * 60 + minute,
            transitionSpec = {
                (slideInVertically(tween(300)) { it } + fadeIn(tween(300))) togetherWith
                    (slideOutVertically(tween(200)) { -it } + fadeOut(tween(160)))
            },
            label = "reminderTime"
        ) { v ->
            val h = v / 60
            val h12 = if (h % 12 == 0) 12 else h % 12
            Row {
                Txt(
                    "%d:%02d".format(h12, v % 60),
                    size = 60.sp, weight = FontWeight.Bold,
                    modifier = Modifier.alignByBaseline()
                )
                Spacer(Modifier.width(10.dp))
                Txt(
                    if (h >= 12) "PM" else "AM",
                    size = 22.sp, weight = FontWeight.SemiBold, color = Ink.accent,
                    modifier = Modifier.alignByBaseline()
                )
            }
        }
        Spacer(Modifier.height(14.dp))

        // 3 · supporting line
        Txt("A nudge to plan tomorrow, every day.", size = 14.sp, color = Ink.muted)
    }
}
