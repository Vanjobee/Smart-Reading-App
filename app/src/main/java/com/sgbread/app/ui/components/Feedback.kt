package com.sgbread.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sgbread.app.audio.AudioManager
import com.sgbread.app.ui.theme.CorrectGreen
import com.sgbread.app.ui.theme.CreamWhite
import com.sgbread.app.ui.theme.IncorrectRed

/** Whether the last answer in an activity was right, wrong, or nothing has been tried yet. */
sealed interface AnswerFeedback {
    data object None : AnswerFeedback
    data class Correct(val message: String) : AnswerFeedback
    data class Incorrect(val message: String) : AnswerFeedback
}

/**
 * A friendly banner with a check mark and praise for correct answers, or a
 * gentle nudge to try again for incorrect ones. Never scolds.
 */
@Composable
fun ReinforcementBanner(
    feedback: AnswerFeedback,
    audio: AudioManager? = null,
    playAudio: Boolean = true,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(feedback, playAudio) {
        val message = when (feedback) {
            is AnswerFeedback.Correct -> feedback.message
            is AnswerFeedback.Incorrect -> feedback.message
            AnswerFeedback.None -> null
        }
        if (playAudio && message != null) audio?.playRecordedPrompt(message)
    }
    AnimatedVisibility(
        visible = feedback !is AnswerFeedback.None,
        enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
        exit = scaleOut(),
        modifier = modifier
    ) {
        val (bg, icon, text) = when (feedback) {
            is AnswerFeedback.Correct -> Triple(CorrectGreen, Icons.Filled.Check, feedback.message)
            is AnswerFeedback.Incorrect -> Triple(IncorrectRed, Icons.Filled.Refresh, feedback.message)
            AnswerFeedback.None -> Triple(Color.Transparent, Icons.Filled.Check, "")
        }
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .widthIn(max = 380.dp)
                .background(bg, RoundedCornerShape(24.dp))
                .padding(horizontal = 24.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = CreamWhite)
                AutoSizeText(
                    text = text,
                    style = MaterialTheme.typography.titleLarge.copy(color = CreamWhite, fontWeight = FontWeight.Bold),
                    maxLines = 2,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
