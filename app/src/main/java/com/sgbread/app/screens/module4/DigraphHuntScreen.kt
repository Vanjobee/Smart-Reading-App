package com.sgbread.app.screens.module4

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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

// Patterns with at least two example words make for a meaningful hunt.
private val huntPatterns = listOf("SH", "CH", "EE")
private val huntWords = LettersBank.patternWords.distinctBy { it.word }

@Composable
fun DigraphHuntScreen(audio: AudioManager, onComplete: () -> Unit, onBack: () -> Unit) {
    var roundIndex by remember { mutableStateOf(0) }
    var found by remember(roundIndex) { mutableStateOf(setOf<String>()) }
    var feedback by remember { mutableStateOf<AnswerFeedback>(AnswerFeedback.None) }
    var finished by remember { mutableStateOf(false) }

    val targetPattern = huntPatterns[roundIndex]
    val board = remember(roundIndex) { huntWords.shuffled() }
    val targetWords = board.filter { it.pattern == targetPattern }.map { it.word }.toSet()

    fun speakPrompt() = audio.speak("Find all the words with $targetPattern.", rate = 0.9f)
    LaunchedEffect(roundIndex) { speakPrompt() }

    LaunchedEffect(feedback) {
        if (feedback !is AnswerFeedback.None) {
            delay(700)
            feedback = AnswerFeedback.None
        }
    }

    fun onWordTap(word: String) {
        if (word in found) return
        if (word in targetWords) {
            audio.playSfx(Sfx.CORRECT)
            found = found + word
            if (found.size == targetWords.size) {
                feedback = AnswerFeedback.Correct(Praise.randomCorrect())
            }
        } else {
            audio.playSfx(Sfx.INCORRECT)
            feedback = AnswerFeedback.Incorrect("That one doesn't have $targetPattern")
        }
    }

    LaunchedEffect(found) {
        if (targetWords.isNotEmpty() && found.size == targetWords.size) {
            delay(1000)
            if (roundIndex == huntPatterns.lastIndex) {
                audio.playSfx(Sfx.HARVEST)
                finished = true
            } else {
                roundIndex += 1
            }
        }
    }

    ActivityScaffold(
        title = "Digraph Hunt",
        onBack = onBack,
        onReplayInstructions = { speakPrompt() },
        feedback = feedback
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Round ${roundIndex + 1} of ${huntPatterns.size}", style = MaterialTheme.typography.bodyMedium)
            Text(
                "Find every word with \"$targetPattern\" (${found.size} / ${targetWords.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(board.size) { i ->
                    val w = board[i]
                    PictureChoiceCard(
                        icon = w.icon,
                        label = w.word,
                        state = if (w.word in found) ChoiceState.CORRECT else ChoiceState.IDLE,
                        enabled = w.word !in found,
                        onClick = { onWordTap(w.word) }
                    )
                }
            }
        }

        if (finished) {
            ActivityCompleteOverlay(onContinue = onComplete)
        }
    }
}
