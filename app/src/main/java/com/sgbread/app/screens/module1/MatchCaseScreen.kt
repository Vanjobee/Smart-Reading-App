package com.sgbread.app.screens.module1

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sgbread.app.audio.AudioManager
import com.sgbread.app.audio.Sfx
import com.sgbread.app.data.LettersBank
import com.sgbread.app.data.PhonicsItem
import com.sgbread.app.ui.components.ActivityCompleteOverlay
import com.sgbread.app.ui.components.ActivityScaffold
import com.sgbread.app.ui.components.AnswerFeedback
import com.sgbread.app.ui.components.ChoiceState
import com.sgbread.app.ui.components.LetterChip
import com.sgbread.app.ui.components.PictureChoiceCard
import kotlinx.coroutines.delay

private val matchTargets = LettersBank.phonicsItems.take(4) // A, B, C, D

@Composable
fun MatchCaseScreen(audio: AudioManager, onComplete: () -> Unit, onBack: () -> Unit) {
    val uppers = remember { matchTargets.map { it.letter.uppercaseChar() }.shuffled() }
    val lowers = remember { matchTargets.map { it.letter.lowercaseChar() }.shuffled() }

    var matched by remember { mutableStateOf(setOf<Char>()) } // stores uppercase base letter
    var selectedUpper by remember { mutableStateOf<Char?>(null) }
    var selectedLower by remember { mutableStateOf<Char?>(null) }
    var feedback by remember { mutableStateOf<AnswerFeedback>(AnswerFeedback.None) }
    var reveal by remember { mutableStateOf<PhonicsItem?>(null) }
    var finished by remember { mutableStateOf(false) }

    fun instructions() = audio.speak("Match the uppercase letter to its lowercase pair.", rate = 0.9f)
    LaunchedEffect(Unit) { instructions() }

    fun evaluate(upper: Char, lower: Char) {
        val base = upper.uppercaseChar()
        if (base == lower.uppercaseChar()) {
            audio.playSfx(Sfx.CORRECT)
            matched = matched + base
            val item = matchTargets.first { it.letter.uppercaseChar() == base }
            reveal = item
            audio.speak("${item.letter}. ${item.word}.")
            feedback = AnswerFeedback.Correct("Great Job!")
        } else {
            audio.playSfx(Sfx.INCORRECT)
            feedback = AnswerFeedback.Incorrect("Try again!")
        }
        selectedUpper = null
        selectedLower = null
    }

    LaunchedEffect(feedback) {
        if (feedback !is AnswerFeedback.None) {
            delay(900)
            feedback = AnswerFeedback.None
        }
    }

    LaunchedEffect(reveal) {
        if (reveal != null) {
            delay(1400)
            reveal = null
            if (matched.size == matchTargets.size) {
                audio.playSfx(Sfx.HARVEST)
                finished = true
            }
        }
    }

    ActivityScaffold(
        title = "Match Upper & Lowercase",
        onBack = onBack,
        onReplayInstructions = { instructions() },
        feedback = feedback
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(padding).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Match each pair", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    uppers.forEach { c ->
                        val base = c.uppercaseChar()
                        val isMatched = base in matched
                        LetterChip(
                            letter = if (isMatched) ' ' else c,
                            state = if (selectedUpper == c) ChoiceState.SELECTED else ChoiceState.IDLE,
                            enabled = !isMatched,
                            onClick = {
                                selectedUpper = c
                                audio.playSfx(Sfx.TAP)
                                val lower = selectedLower
                                if (lower != null) evaluate(c, lower)
                            }
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    lowers.forEach { c ->
                        val base = c.uppercaseChar()
                        val isMatched = base in matched
                        LetterChip(
                            letter = if (isMatched) ' ' else c,
                            state = if (selectedLower == c) ChoiceState.SELECTED else ChoiceState.IDLE,
                            enabled = !isMatched,
                            onClick = {
                                selectedLower = c
                                audio.playSfx(Sfx.TAP)
                                val upper = selectedUpper
                                if (upper != null) evaluate(upper, c)
                            }
                        )
                    }
                }
            }

            AnimatedVisibility(visible = reveal != null) {
                reveal?.let { item ->
                    Box(modifier = Modifier.padding(top = 24.dp)) {
                        PictureChoiceCard(icon = item.icon, label = item.word, onClick = {})
                    }
                }
            }
        }

        if (finished) {
            ActivityCompleteOverlay(onContinue = onComplete)
        }
    }
}
