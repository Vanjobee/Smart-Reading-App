package com.sgbread.app.screens.module2

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
import com.sgbread.app.data.LettersBank
import com.sgbread.app.data.Praise
import com.sgbread.app.ui.components.ActivityCompleteOverlay
import com.sgbread.app.ui.components.ActivityScaffold
import com.sgbread.app.ui.components.AnswerFeedback
import com.sgbread.app.ui.components.ChoiceState
import com.sgbread.app.ui.components.PictureChoiceCard
import kotlinx.coroutines.delay

private val rounds = LettersBank.phonicsItems.shuffled().take(6)

@Composable
fun ListenMatchScreen(audio: AudioManager, onComplete: () -> Unit, onBack: () -> Unit) {
    var roundIndex by remember { mutableStateOf(0) }
    var feedback by remember { mutableStateOf<AnswerFeedback>(AnswerFeedback.None) }
    var wrongPick by remember { mutableStateOf<Int?>(null) }
    var finished by remember { mutableStateOf(false) }

    val round = rounds[roundIndex]
    val choices = remember(roundIndex) {
        (listOf(round) + LettersBank.phonicsItems.filter { it != round }.shuffled().take(2)).shuffled()
    }

    fun speakPrompt() = audio.speak("Listen. The letter ${round.letter}.", rate = 0.85f)
    LaunchedEffect(roundIndex) {
        wrongPick = null
        speakPrompt()
    }

    fun onPick(index: Int) {
        val picked = choices[index]
        if (picked.letter == round.letter) {
            audio.playSfx(Sfx.CORRECT)
            feedback = AnswerFeedback.Correct(Praise.randomCorrect())
        } else {
            audio.playSfx(Sfx.INCORRECT)
            wrongPick = index
            feedback = AnswerFeedback.Incorrect(Praise.randomEncouragement())
        }
    }

    LaunchedEffect(feedback) {
        val current = feedback
        if (current is AnswerFeedback.Correct) {
            delay(1000)
            feedback = AnswerFeedback.None
            if (roundIndex == rounds.lastIndex) {
                audio.playSfx(Sfx.HARVEST)
                finished = true
            } else {
                roundIndex += 1
            }
        } else if (current is AnswerFeedback.Incorrect) {
            delay(1000)
            feedback = AnswerFeedback.None
        }
    }

    ActivityScaffold(
        title = "Listen and Match",
        onBack = onBack,
        onReplayInstructions = { speakPrompt() },
        feedback = feedback
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Round ${roundIndex + 1} of ${rounds.size}", style = MaterialTheme.typography.bodyMedium)
            Text(
                "Which picture starts with \"${round.letter}\"?",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                choices.forEachIndexed { i, item ->
                    PictureChoiceCard(
                        icon = item.icon,
                        label = null,
                        state = if (wrongPick == i) ChoiceState.WRONG else ChoiceState.IDLE,
                        onClick = { onPick(i) }
                    )
                }
            }
        }

        if (finished) {
            ActivityCompleteOverlay(onContinue = onComplete)
        }
    }
}
