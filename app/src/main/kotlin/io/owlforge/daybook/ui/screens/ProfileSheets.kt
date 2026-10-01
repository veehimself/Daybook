package io.owlforge.daybook.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.owlforge.daybook.ui.Avatar
import io.owlforge.daybook.ui.Capsule
import io.owlforge.daybook.ui.Fonts
import io.owlforge.daybook.ui.Ink
import io.owlforge.daybook.ui.TimeWheelValue
import io.owlforge.daybook.ui.Txt
import io.owlforge.daybook.ui.avatarPresets
import io.owlforge.daybook.ui.bounceClick
import io.owlforge.daybook.util.fmtMinutes

@Composable
fun ColumnScope.NameSheetContent(initial: String, onSave: (String) -> Unit) {
    var name by remember { mutableStateOf(initial) }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }

    Txt("Your name", size = 26.sp, weight = FontWeight.Bold)
    Spacer(Modifier.height(16.dp))
    BasicTextField(
        value = name,
        onValueChange = { name = it.take(24) },
        singleLine = true,
        textStyle = TextStyle(
            color = Ink.text, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, fontFamily = Fonts.sans
        ),
        cursorBrush = SolidColor(Ink.accent),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Ink.line, RoundedCornerShape(18.dp))
            .padding(18.dp)
            .focusRequester(focus)
    )
    Spacer(Modifier.height(20.dp))
    Capsule("Save", Ink.accent, Modifier.fillMaxWidth(), filled = true) {
        if (name.isNotBlank()) onSave(name.trim())
    }
}

@Composable
fun ColumnScope.TimeSheetContent(hour: Int, minute: Int, onSave: (Int, Int) -> Unit) {
    var v by remember { mutableIntStateOf(hour * 60 + minute) }

    Txt("Plan-tomorrow reminder", size = 26.sp, weight = FontWeight.Bold)
    Txt(
        "You'll get a nudge at ${fmtMinutes(v)} every day.",
        size = 14.sp, color = Ink.muted,
        modifier = Modifier.padding(top = 6.dp, bottom = 12.dp)
    )
    TimeWheelValue(v) { v = it }
    Spacer(Modifier.height(20.dp))
    Capsule("Save", Ink.accent, Modifier.fillMaxWidth(), filled = true) { onSave(v / 60, v % 60) }
}

@Composable
fun ColumnScope.AvatarSheetContent(current: String, onPick: (String) -> Unit, onPhoto: () -> Unit) {
    Txt("Pick an avatar", size = 26.sp, weight = FontWeight.Bold)
    Spacer(Modifier.height(16.dp))
    avatarPresets.indices.chunked(4).forEach { row ->
        Row(
            Modifier.fillMaxWidth().padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            row.forEach { idx ->
                val code = "preset:$idx"
                Box(
                    Modifier
                        .size(68.dp)
                        .bounceClick { onPick(code) }
                        .border(2.dp, if (current == code) Ink.accent else Color.Transparent, CircleShape)
                        .padding(4.dp)
                ) { Avatar(code, 60.dp) }
            }
        }
    }
    Spacer(Modifier.height(14.dp))
    Capsule("Choose a photo", Ink.accent, Modifier.fillMaxWidth()) { onPhoto() }
}
