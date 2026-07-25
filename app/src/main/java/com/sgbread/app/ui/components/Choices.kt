package com.sgbread.app.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.sgbread.app.data.FarmIconKey
import com.sgbread.app.ui.icons.FarmIcon
import com.sgbread.app.ui.theme.CorrectGreen
import com.sgbread.app.ui.theme.CreamWhite
import com.sgbread.app.ui.theme.IncorrectRed
import com.sgbread.app.ui.theme.RiceGreenDark
import com.sgbread.app.ui.theme.SunOrange
import com.sgbread.app.ui.theme.TextBrown

enum class ChoiceState { IDLE, SELECTED, CORRECT, WRONG }

private fun ChoiceState.borderColor(): Color = when (this) {
    ChoiceState.IDLE -> Color(0x408B5E34)
    ChoiceState.SELECTED -> SunOrange
    ChoiceState.CORRECT -> CorrectGreen
    ChoiceState.WRONG -> IncorrectRed
}

private fun ChoiceState.cardColor(): Color = when (this) {
    ChoiceState.IDLE -> CreamWhite
    ChoiceState.SELECTED -> Color(0xFFFFF1C2)
    ChoiceState.CORRECT -> Color(0xFFE8F7DF)
    ChoiceState.WRONG -> Color(0xFFFFE1DF)
}

/** A tappable card showing a picture and optional caption, used across matching games.
 * Pass [image] for a real drawable photo (e.g. the A-Z phonics pictures), or [icon] for
 * the hand-drawn farm vector set; [image] takes priority when both are supplied. */
@Composable
fun PictureChoiceCard(
    icon: FarmIconKey? = null,
    image: Int? = null,
    label: String? = null,
    state: ChoiceState = ChoiceState.IDLE,
    enabled: Boolean = true,
    imageSize: androidx.compose.ui.unit.Dp = 72.dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .widthIn(min = imageSize + 32.dp)
            .shadow(if (state == ChoiceState.IDLE) 4.dp else 8.dp, RoundedCornerShape(22.dp))
            .background(state.cardColor(), RoundedCornerShape(22.dp))
            .border(3.dp, state.borderColor(), RoundedCornerShape(22.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (image != null) {
            Image(
                painter = painterResource(image),
                contentDescription = label,
                modifier = Modifier.size(imageSize),
                contentScale = ContentScale.Fit
            )
        } else if (icon != null) {
            FarmIcon(icon, modifier = Modifier.size(imageSize), background = null)
        }
        if (label != null) {
            Text(
                label,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

/** The drop target ("basket") a letter chip or tile is dragged into, sitting visually
 * between the question picture and the answer choices in drag-to-match games. Border
 * color reflects [state] the same way [PictureChoiceCard] does (idle/hover/correct). */
@Composable
fun BasketDropTarget(
    state: ChoiceState = ChoiceState.IDLE,
    size: androidx.compose.ui.unit.Dp = 96.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(if (state == ChoiceState.IDLE) 3.dp else 8.dp, RoundedCornerShape(22.dp))
            .background(state.cardColor(), RoundedCornerShape(22.dp))
            .border(3.dp, state.borderColor(), RoundedCornerShape(22.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        FarmIcon(FarmIconKey.BASKET, modifier = Modifier.fillMaxSize(), background = null)
    }
}

/** A large tappable letter tile, used for letter choices, blanks, and tile pools.
 * [fontSize] defaults to the chip's usual headlineMedium size; pass a larger value to
 * scale the glyph up independently of [size] (e.g. Module 1's enlarged match screen). */
@Composable
fun LetterChip(
    letter: Char,
    state: ChoiceState = ChoiceState.IDLE,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    size: androidx.compose.ui.unit.Dp = 64.dp,
    fontSize: androidx.compose.ui.unit.TextUnit = androidx.compose.ui.unit.TextUnit.Unspecified,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(if (state == ChoiceState.IDLE) 3.dp else 7.dp, RoundedCornerShape(16.dp))
            .background(if (letter == ' ') Color(0x22FFFFFF) else state.cardColor(), RoundedCornerShape(16.dp))
            .border(3.dp, state.borderColor(), RoundedCornerShape(16.dp))
            .then(if (onClick != null) Modifier.clickable(enabled = enabled, onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        if (letter != ' ') {
            Text(
                letter.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontSize = fontSize,
                fontWeight = FontWeight.ExtraBold,
                color = TextBrown
            )
        }
    }
}

/** Circular icon button used for module/activity tiles on the home and module-select screens. */
@Composable
fun FarmTile(
    icon: FarmIconKey,
    title: String,
    subtitle: String? = null,
    completed: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(26.dp))
            .background(CreamWhite.copy(alpha = 0.96f), RoundedCornerShape(26.dp))
            .border(3.dp, if (completed) CorrectGreen else Color(0x338B5E34), RoundedCornerShape(26.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .background(RiceGreenDark.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            FarmIcon(icon, modifier = Modifier.size(64.dp), background = null)
        }
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        if (subtitle != null) {
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

/**
 * A microphone button that triggers the AudioManager's speech recognition.
 */
@Composable
fun SpeechAnalysisButton(
    targetText: String,
    audio: com.sgbread.app.audio.AudioManager,
    onResult: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    promptText: String = "Say it now"
) {
    var isListening by remember { mutableStateOf(false) }
    val context = LocalContext.current

    fun beginListening() {
        isListening = true
        audio.playWord(targetText, onComplete = {
            audio.startListening(targetText) { success ->
                isListening = false
                onResult(success)
            }
        })
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) beginListening() }

    val infiniteTransition = rememberInfiniteTransition(label = "micPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = promptText,
            style = MaterialTheme.typography.labelLarge,
            color = TextBrown,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Box(
            modifier = Modifier
                .size(72.dp)
                .scale(scale)
                .background(if (isListening) SunOrange else RiceGreenDark, CircleShape)
                .clickable(enabled = !isListening) {
                    val hasMicPermission = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasMicPermission) {
                        beginListening()
                    } else {
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Speak now",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}
