package io.owlforge.daybook.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.owlforge.daybook.data.PlanTask
import io.owlforge.daybook.data.Profile
import io.owlforge.daybook.data.TaskStatus
import io.owlforge.daybook.ui.screens.AddTaskContent
import io.owlforge.daybook.ui.screens.HomeScreen
import io.owlforge.daybook.ui.screens.JournalScreen
import io.owlforge.daybook.ui.screens.Onboarding
import io.owlforge.daybook.ui.screens.ProfileScreen
import io.owlforge.daybook.ui.screens.TaskDetailContent
import kotlinx.coroutines.delay
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

@Composable
fun DaybookRoot(vm: MainViewModel) {
    val profile by vm.profile.collectAsStateWithLifecycle()
    val stage = when {
        profile == null -> 0
        profile?.onboarded == false -> 1
        else -> 2
    }
    Box(Modifier.fillMaxSize().background(Ink.bg)) {
        AuroraBackground()
        Crossfade(stage, animationSpec = tween(400), label = "root") { s ->
            when (s) {
                1 -> OnboardingGate(vm)
                2 -> profile?.let { Shell(vm, it) }
            }
        }
    }
}

@Composable
private fun OnboardingGate(vm: MainViewModel) {
    var pending by remember { mutableStateOf<Triple<String, Int, Int>?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        // Save regardless of the answer; the profile (and alarms) must persist either way.
        pending?.let { vm.saveProfile(it.first, it.second, it.third) }
    }
    Onboarding { n, h, m ->
        pending = Triple(n, h, m)
        if (Build.VERSION.SDK_INT >= 33) launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        else vm.saveProfile(n, h, m)
    }
}

@Composable
private fun Shell(vm: MainViewModel, p: Profile) {
    val tab by vm.tab.collectAsStateWithLifecycle()
    val showAdd by vm.showAdd.collectAsStateWithLifecycle()
    val openTask by vm.openTask.collectAsStateWithLifecycle()

    // Keep the last task around so the sheet can finish its exit animation.
    val lastTask = remember { mutableStateOf<PlanTask?>(null) }
    openTask?.let { lastTask.value = it }

    var party by remember { mutableIntStateOf(0) }
    var confettiLive by remember { mutableStateOf(false) }
    LaunchedEffect(party) { if (party > 0) { delay(3500); confettiLive = false } }

    Box(Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = tab,
            transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(150)) },
            label = "tabs"
        ) { t ->
            when (t) {
                0 -> HomeScreen(vm, p.name)
                1 -> JournalScreen(vm)
                else -> ProfileScreen(vm, p)
            }
        }

        if (tab == 0) {
            val bob by rememberInfiniteTransition(label = "fab").animateFloat(
                -6f, 6f, infiniteRepeatable(tween(1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                label = "bob"
            )
            Capsule(
                "＋  Plan a task", Ink.violet, filled = true,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 84.dp)
                    .graphicsLayer { translationY = bob }
            ) { vm.showAdd.value = true }
        }

        BottomNav(tab, Modifier.align(Alignment.BottomCenter)) { vm.tab.value = it }

        BottomSheet(showAdd, { vm.showAdd.value = false }) {
            AddTaskContent(vm.dayOffset.value) { title, s, e, cb -> vm.addTask(title, s, e, cb) }
        }

        BottomSheet(openTask != null, { vm.openTaskId.value = null }) {
            lastTask.value?.let { t ->
                TaskDetailContent(
                    t,
                    onComplete = {
                        vm.mark(t.id, TaskStatus.COMPLETED)
                        vm.openTaskId.value = null
                        confettiLive = true; party++
                    },
                    onIncomplete = {
                        vm.mark(t.id, TaskStatus.INCOMPLETE)
                        vm.openTaskId.value = null
                    },
                )
            }
        }

        if (confettiLive) key(party) { Celebration() }
    }
}

@Composable
private fun Celebration() {
    val parties = remember {
        listOf(
            Party(
                speed = 0f, maxSpeed = 32f, damping = 0.9f, spread = 360,
                colors = listOf(0x7C5CFF, 0x3DFFB5, 0xFF5C7A, 0xFFC857),
                position = Position.Relative(0.5, 0.35),
                emitter = Emitter(duration = 150, TimeUnit.MILLISECONDS).max(120)
            )
        )
    }
    KonfettiView(Modifier.fillMaxSize(), parties = parties)
}

@Composable
private fun BottomNav(selected: Int, modifier: Modifier, onSelect: (Int) -> Unit) {
    val items = listOf("Plan", "Journal", "You")
    Row(
        modifier
            .navigationBarsPadding()
            .padding(bottom = 12.dp)
            .background(Ink.surface, CircleShape)
            .border(1.dp, Ink.line, CircleShape)
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items.forEachIndexed { i, label ->
            val on = i == selected
            val bg by animateColorAsState(if (on) Ink.violet else Ink.surface, tween(250), label = "navbg")
            Box(
                Modifier
                    .background(bg, CircleShape)
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onSelect(i) }
                    .padding(horizontal = 22.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Txt(label, weight = FontWeight.SemiBold, size = 14.sp, color = if (on) Ink.text else Ink.muted)
            }
        }
    }
}
