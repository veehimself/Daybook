package io.owlforge.daybook.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AvatarPreset(val emoji: String, val color: Color)

val avatarPresets = listOf(
    AvatarPreset("🦊", Color(0xFFE9A06B)),
    AvatarPreset("🐼", Color(0xFFB9C3CC)),
    AvatarPreset("🐙", Color(0xFFD175D4)),
    AvatarPreset("🦉", Color(0xFFC9A86A)),
    AvatarPreset("🐸", Color(0xFF8FC48A)),
    AvatarPreset("🚀", Color(0xFF6FA8DC)),
    AvatarPreset("🌙", Color(0xFF8E9AD1)),
    AvatarPreset("🔥", Color(0xFFE5735A)),
    AvatarPreset("🎧", Color(0xFF6CC5B8)),
    AvatarPreset("🌵", Color(0xFFA6C48A)),
    AvatarPreset("🍜", Color(0xFFE7C26A)),
    AvatarPreset("⚡", Color(0xFFE8D36A)),
)

/** [code] is "preset:<index>" or "photo:<timestamp>". */
@Composable
fun Avatar(code: String, size: Dp, modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    val isPhoto = code.startsWith("photo:")
    val photo by produceState<ImageBitmap?>(null, code) {
        value = if (isPhoto) withContext(Dispatchers.IO) {
            BitmapFactory.decodeFile(File(ctx.filesDir, "avatar.jpg").path)?.asImageBitmap()
        } else null
    }
    val preset = remember(code) {
        code.removePrefix("preset:").toIntOrNull()?.let { avatarPresets.getOrNull(it) } ?: avatarPresets[0]
    }
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(if (isPhoto) Ink.card else preset.color),
        contentAlignment = Alignment.Center
    ) {
        val img = photo
        if (isPhoto) {
            if (img != null) {
                Image(
                    bitmap = img, contentDescription = null,
                    contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            Txt(preset.emoji, size = (size.value * 0.5f).sp)
        }
    }
}
