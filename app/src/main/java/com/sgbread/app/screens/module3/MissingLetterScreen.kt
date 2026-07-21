package com.sgbread.app.screens.module3

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.sgbread.app.ui.components.LetterChip
import com.sgbread.app.ui.icons.FarmIcon
import kotlinx.coroutines.delay

private val rounds = LettersBank.blendWords.shuffled().take(6)

@Composable
fun MissingLetterScreen(audio: AudioManager, onComplete: () -> Unit, onBack: () -> Unit) {
    var roundIndex by remember { mutableStateOf(0) }
    var feedback by remember { mutableStateOf<AnswerFeedback>(AnswerFeedback.None) }
    var wrongLetter by remember { mutableStateOf<Char?>(null) }
    var finished by remember { mutableStateOf(false) }

    val round = rounds[roundIndex]
    val blankIndex = remember(roundIndex) { round.word.indices.random() }
    val missingLetter = round.word[blankIndex]
    val choices = remember(roundIndex) {
        val distractors = ('a'..'z').filter { it != missingLetter }.shuffled().take(2)
        (distractors + missingLetter).shuffled()
    }

    fun speakPrompt() = audio.speak("Complete the word. ${round.word}.", rate = 0.85f)
    LaunchedEffect(roundIndex) {
        wrongLetter = null
        speakPrompt()
    }

    fun onPick(letter: Char) {
        if (letter == missingLetter) {
            audio.playSfx(Sfx.CORRECT)
            feedback = AnswerFeedback.Correct(Praise.randomCorrect())
        } else {
            audio.playSfx(Sfx.INCORRECT)
            wrongLetter = letter
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
        title = "Supply the Missing Letter",
        onBack = onBack,
        onReplayInstructions = { speakPrompt() },
        feedback = feedback
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Word ${roundIndex + 1} of ${rounds.size}", style = MaterialTheme.typography.bodyMedium)
            FarmIcon(round.icon, modifier = Modifier.size(120.dp))

            Row(modifier = Modifier.padding(vertical = 16.dp), horizontalArrangement = Arrangement.Center) {
                round.word.forEachIndexed { i, c ->
                    LetterChip(letter = if (i == blankIndex) ' ' else c, modifier = Modifier.padding(4.dp))
                }
            }

            Text("Which letter is missing?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                choices.forEach { c ->
                    LetterChip(
                        letter = c,
                        state = if (wrongLetter == c) ChoiceState.WRONG else ChoiceState.IDLE,
                        onClick = { onPick(c) }
                    )
                }
            }
        }

        if (finished) {
            ActivityCompleteOverlay(onContinue = onComplete)
        }
    }
}
