package com.sgbread.app.screens.module4

import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sgbread.app.audio.AudioManager
import com.sgbread.app.audio.Sfx
import com.sgbread.app.data.LettersBank
import com.sgbread.app.data.Praise
import com.sgbread.app.ui.components.ActivityCompleteOverlay
import com.sgbread.app.ui.components.ActivityScaffold
import com.sgbread.app.ui.components.AnswerFeedback
import com.sgbread.app.ui.icons.FarmIcon
import kotlinx.coroutines.delay

private val rounds = LettersBank.patternWords.distinctBy { it.word }.shuffled().take(6)

@Composable
fun PictureWordMatchScreen(audio: AudioManager, onComplete: () -> Unit, onBack: () -> Unit) {
    var roundIndex by remember { mutableStateOf(0) }
    var feedback by remember { mutableStateOf<AnswerFeedback>(AnswerFeedback.None) }
    var wrongWord by remember { mutableStateOf<String?>(null) }
    var finished by remember { mutableStateOf(false) }

    val round = rounds[roundIndex]
    val choices = remember(roundIndex) {
        val distractors = rounds.filter { it.word != round.word }.shuffled().take(2).map { it.word }
        (distractors + round.word).shuffled()
    }

    fun speakWord() = audio.speak(round.word, rate = 0.85f)
    LaunchedEffect(roundIndex) {
        wrongWord = null
        speakWord()
    }

    fun onPick(word: String) {
        if (word == round.word) {
            audio.playSfx(Sfx.CORRECT)
            feedback = AnswerFeedback.Correct(Praise.randomCorrect())
        } else {
            audio.playSfx(Sfx.INCORRECT)
            wrongWord = word
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
        title = "Picture-to-Word Match",
        onBack = onBack,
        onReplayInstructions = { speakWord() },
        feedback = feedback
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(padding).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Word ${roundIndex + 1} of ${rounds.size}", style = MaterialTheme.typography.bodyMedium)
            Text(
                "Tap the picture to hear it, then pick the spelling",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            FarmIcon(
                round.icon,
                modifier = Modifier
                    .size(100.dp)
                    .clickable { speakWord() }
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                choices.forEach { word ->
                    OutlinedButton(
                        onClick = { onPick(word) },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (wrongWord == word) Color(0x33E57373) else Color.White
                        )
                    ) {
                        Text(word, style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }

        if (finished) {
            ActivityCompleteOverlay(onContinue = onComplete)
        }
    }
}
