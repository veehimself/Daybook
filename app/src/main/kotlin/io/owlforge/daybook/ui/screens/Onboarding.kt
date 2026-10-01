package io.owlforge.daybook.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.owlforge.daybook.ui.Capsule
import io.owlforge.daybook.ui.Ink
import io.owlforge.daybook.ui.TimeWheel
import io.owlforge.daybook.ui.Txt
import io.owlforge.daybook.ui.entrance

@Composable
fun Onboarding(onDone: (name: String, hour: Int, minute: Int) -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }
    var h by remember { mutableIntStateOf(21) }
    var m by remember { mutableIntStateOf(0) }
    val canGo = step == 1 || name.isNotBlank()

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(28.dp)
    ) {
        Spacer(Modifier.weight(0.4f))
        AnimatedContent(
            targetState = step,
            transitionSpec = {
                (slideInHorizontally(spring(dampingRatio = 0.8f, stiffness = 300f)) { it } + fadeIn()) togetherWith
                    (slideOutHorizontally { -it } + fadeOut())
            },
            label = "onboarding"
        ) { s ->
            if (s == 0) NameStep(name) { name = it } else TimeStep(h, m) { hh, mm -> h = hh; m = mm }
        }
        Spacer(Modifier.weight(1f))
        Capsule(
            label = if (step == 0) "Next" else "Let's go",
            color = Ink.violet,
            filled = true,
            modifier = Modifier.fillMaxWidth().alpha(if (canGo) 1f else 0.4f),
        ) {
            if (step == 0) { if (name.isNotBlank()) step = 1 } else onDone(name.trim(), h, m)
        }
    }
}

@Composable
private fun NameStep(name: String, onName: (String) -> Unit) {
    val wave by rememberInfiniteTransition(label = "wave").animateFloat(
        -14f, 14f, infiniteRepeatable(tween(650), RepeatMode.Reverse), label = "w"
    )
    Column {
        Txt(
            "👋", size = 64.sp,
            modifier = Modifier.graphicsLayer { rotationZ = wave; transformOrigin = TransformOrigin(0.7f, 0.8f) }
        )
        Spacer(Modifier.height(16.dp))
        Txt("Hey there.", size = 40.sp, weight = FontWeight.Bold, modifier = Modifier.entrance(0))
        Txt("What should I call you?", size = 20.sp, color = Ink.muted, modifier = Modifier.entrance(1))
        Spacer(Modifier.height(28.dp))
        BasicTextField(
            value = name,
            onValueChange = onName,
            singleLine = true,
            textStyle = TextStyle(color = Ink.text, fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
            cursorBrush = SolidColor(Ink.violet),
            decorationBox = { inner ->
                Column {
                    if (name.isEmpty()) Txt("Your name", size = 22.sp, color = Ink.muted)
                    inner()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Ink.line, RoundedCornerShape(20.dp))
                .padding(20.dp)
                .entrance(2)
        )
    }
}

@Composable
private fun TimeStep(h: Int, m: Int, onChange: (Int, Int) -> Unit) {
    Column {
        Txt("When do you plan\ntomorrow?", size = 34.sp, weight = FontWeight.Bold, modifier = Modifier.entrance(0))
        Spacer(Modifier.height(8.dp))
        Txt("I'll nudge you every day at this time.", size = 16.sp, color = Ink.muted, modifier = Modifier.entrance(1))
        Spacer(Modifier.height(24.dp))
        TimeWheel(h, m, onChange)
    }
}
