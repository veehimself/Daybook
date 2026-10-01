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
                "👋", size = 40.sp,
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
    val shape = RoundedCornerShape(36.dp)
    Column(
        modifier
            .fillMaxWidth()
            .bounceClick(onClick)
            .background(
                Brush.linearGradient(listOf(Color.White.copy(alpha = 0.16f), Color.White.copy(alpha = 0.05f))),
                shape
            )
            .border(1.5.dp, Ink.text.copy(alpha = 0.85f), shape)
            .padding(20.dp)
    ) {
        Row(
            Modifier
                .background(Ink.accent.copy(alpha = 0.16f), CircleShape)
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ClockIcon(Ink.accent, 16.dp)
            Spacer(Modifier.width(8.dp))
            Txt("Daily “plan tomorrow” reminder", size = 14.sp, color = Ink.accent, weight = FontWeight.Medium)
        }
        Spacer(Modifier.height(24.dp))
        AnimatedContent(
            targetState = fmtHm(hour, minute),
            transitionSpec = {
                (slideInVertically(tween(300)) { it } + fadeIn(tween(300))) togetherWith
                    (slideOutVertically(tween(200)) { -it } + fadeOut(tween(160)))
            },
            label = "reminderTime"
        ) { t ->
            Txt(
                t, size = 48.sp, weight = FontWeight.Bold, mono = true,
                align = TextAlign.Center, modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(Modifier.height(10.dp))
        Txt(
            "Tap to change", size = 13.sp, color = Ink.muted,
            align = TextAlign.Center, modifier = Modifier.fillMaxWidth()
        )
    }
}
