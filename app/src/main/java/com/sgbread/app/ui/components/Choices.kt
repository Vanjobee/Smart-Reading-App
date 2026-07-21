package com.sgbread.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    ChoiceState.IDLE -> Color(0x33000000)
    ChoiceState.SELECTED -> SunOrange
    ChoiceState.CORRECT -> CorrectGreen
    ChoiceState.WRONG -> IncorrectRed
}

/** A tappable card showing a farm picture and optional caption, used across matching games. */
@Composable
fun PictureChoiceCard(
    icon: FarmIconKey,
    label: String? = null,
    state: ChoiceState = ChoiceState.IDLE,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(CreamWhite, RoundedCornerShape(20.dp))
            .border(3.dp, state.borderColor(), RoundedCornerShape(20.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FarmIcon(icon, modifier = Modifier.size(72.dp), background = null)
        if (label != null) {
            Text(label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

/** A large tappable letter tile, used for letter choices, blanks, and tile pools. */
@Composable
fun LetterChip(
    letter: Char,
    state: ChoiceState = ChoiceState.IDLE,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(64.dp)
            .background(if (letter == ' ') Color(0x22000000) else CreamWhite, RoundedCornerShape(14.dp))
            .border(3.dp, state.borderColor(), RoundedCornerShape(14.dp))
            .then(if (onClick != null) Modifier.clickable(enabled = enabled, onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        if (letter != ' ') {
            Text(
                letter.toString(),
                style = MaterialTheme.typography.headlineMedium,
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
            .background(CreamWhite, RoundedCornerShape(24.dp))
            .border(3.dp, if (completed) CorrectGreen else Color(0x22000000), RoundedCornerShape(24.dp))
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
