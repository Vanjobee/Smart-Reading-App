package com.sgbread.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
fun ReinforcementBanner(feedback: AnswerFeedback, modifier: Modifier = Modifier) {
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
                .background(bg, RoundedCornerShape(24.dp))
                .padding(horizontal = 24.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = CreamWhite)
                Text(
                    text = text,
                    color = CreamWhite,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
