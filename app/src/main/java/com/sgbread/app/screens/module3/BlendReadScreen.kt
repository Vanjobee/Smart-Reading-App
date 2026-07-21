package com.sgbread.app.screens.module3

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
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
import com.sgbread.app.data.BlendWord
import com.sgbread.app.data.LettersBank
import com.sgbread.app.data.Praise
import com.sgbread.app.ui.components.ActivityCompleteOverlay
import com.sgbread.app.ui.components.ActivityScaffold
import com.sgbread.app.ui.components.AnswerFeedback
import com.sgbread.app.ui.components.ChoiceState
import com.sgbread.app.ui.components.PictureChoiceCard
import kotlinx.coroutines.delay

private val rounds = LettersBank.blendWords.shuffled().take(6)

@Composable
fun BlendReadScreen(audio: AudioManager, onComplete: () -> Unit, onBack: () -> Unit) {
    var roundIndex by remember { mutableStateOf(0) }
    var feedback by remember { mutableStateOf<AnswerFeedback>(AnswerFeedback.None) }
    var wrongWord by remember { mutableStateOf<String?>(null) }
    var finished by remember { mutableStateOf(false) }

    val round = rounds[roundIndex]
    val choices = remember(roundIndex) {
        (listOf(round) + LettersBank.blendWords.filter { it.word != round.word }.shuffled().take(2)).shuffled()
    }

    fun speakPrompt() {
        val sounds = round.word.toCharArray().joinToString(". ") { it.toString() }
        audio.speak("$sounds. ${round.word}.", rate = 0.8f)
    }
    LaunchedEffect(roundIndex) {
        wrongWord = null
        speakPrompt()
    }

    fun onPick(choice: BlendWord) {
        if (choice.word == round.word) {
            audio.playSfx(Sfx.CORRECT)
            feedback = AnswerFeedback.Correct(Praise.randomCorrect())
        } else {
            audio.playSfx(Sfx.INCORRECT)
            wrongWord = choice.word
            feedback = AnswerFeedback.Incorrect(Praise.randomEncouragement())
        }
    }

    LaunchedEffect(feedback) {
        val current = feedback
        if (current is AnswerFeedback.Correct) {
            delay(1100)
            feedback = AnswerFeedback.None
            if (roundIndex == rounds.lastIndex) {
                audio.playSfx(Sfx.HARVEST)
                finished = true
            } else {
                roundIndex += 1
            }
        } else if (current is AnswerFeedback.Incorrect) {
            delay(900)
            feedback = AnswerFeedback.None
        }
    }

    ActivityScaffold(
        title = "Blend and Read",
        onBack = onBack,
        onReplayInstructions = { speakPrompt() },
        feedback = feedback
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(padding).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Word ${roundIndex + 1} of ${rounds.size}", style = MaterialTheme.typography.bodyMedium)
            Text(
                "Listen, then tap the word you heard",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                choices.forEach { choice ->
                    PictureChoiceCard(
                        icon = choice.icon,
                        label = choice.word,
                        state = if (wrongWord == choice.word) ChoiceState.WRONG else ChoiceState.IDLE,
                        onClick = { onPick(choice) }
                    )
                }
            }
        }

        if (finished) {
            ActivityCompleteOverlay(onContinue = onComplete)
        }
    }
}
