package io.owlforge.daybook.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.owlforge.daybook.data.Profile
import io.owlforge.daybook.ui.Capsule
import io.owlforge.daybook.ui.Ink
import io.owlforge.daybook.ui.MainViewModel
import io.owlforge.daybook.ui.TimeWheel
import io.owlforge.daybook.ui.Txt
import io.owlforge.daybook.ui.entrance
import kotlinx.coroutines.delay

@Composable
fun ProfileScreen(vm: MainViewModel, p: Profile) {
    var name by remember(p.name) { mutableStateOf(p.name) }
    var h by remember(p.planHour) { mutableIntStateOf(p.planHour) }
    var m by remember(p.planMinute) { mutableIntStateOf(p.planMinute) }
    var saved by remember { mutableStateOf(false) }
    LaunchedEffect(saved) { if (saved) { delay(1800); saved = false } }

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 130.dp)
    ) {
        Spacer(Modifier.height(20.dp))
        Txt("You", size = 34.sp, weight = FontWeight.Bold, modifier = Modifier.entrance(0))
        Spacer(Modifier.height(24.dp))

        Txt("Name", size = 13.sp, color = Ink.muted)
        Spacer(Modifier.height(8.dp))
        BasicTextField(
            value = name,
            onValueChange = { name = it },
            singleLine = true,
            textStyle = TextStyle(color = Ink.text, fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
            cursorBrush = SolidColor(Ink.accent),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Ink.line, RoundedCornerShape(18.dp))
                .padding(18.dp)
                .entrance(1)
        )

        Spacer(Modifier.height(28.dp))
        Txt("Daily “plan tomorrow” reminder", size = 13.sp, color = Ink.muted)
        Spacer(Modifier.height(8.dp))
        TimeWheel(p.planHour, p.planMinute) { hh, mm -> h = hh; m = mm }

        Spacer(Modifier.height(24.dp))
        Capsule("Save", Ink.accent, Modifier.fillMaxWidth(), filled = true) {
            if (name.isNotBlank()) { vm.saveProfile(name, h, m); saved = true }
        }
        AnimatedVisibility(visible = saved) {
            Txt("Saved ✔", color = Ink.mint, weight = FontWeight.SemiBold, modifier = Modifier.padding(top = 12.dp))
        }
    }
}
