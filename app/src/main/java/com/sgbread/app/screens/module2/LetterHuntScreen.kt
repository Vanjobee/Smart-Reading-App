package com.sgbread.app.screens.module2

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
import com.sgbread.app.data.ALPHABET
import com.sgbread.app.data.LettersBank
import com.sgbread.app.data.Praise
import com.sgbread.app.ui.components.ActivityCompleteOverlay
import com.sgbread.app.ui.components.ActivityScaffold
import com.sgbread.app.ui.components.AnswerFeedback
import com.sgbread.app.ui.components.ChoiceState
import com.sgbread.app.ui.components.LetterChip
import kotlinx.coroutines.delay

private data class HuntCell(val letter: Char, val isTarget: Boolean, var found: Boolean = false)

private val huntTargets = LettersBank.phonicsItems.take(4).map { it.letter } // A, B, C, D
private const val GRID_SIZE = 25
private const val TARGET_COUNT = 5

private fun buildGrid(target: Char): List<HuntCell> {
    val distractors = ALPHABET.filter { it != target }
    val targetCells = List(TARGET_COUNT) { HuntCell(target, true) }
    val fillerCells = List(GRID_SIZE - TARGET_COUNT) { HuntCell(distractors.random(), false) }
    return (targetCells + fillerCells).shuffled()
}

@Composable
fun LetterHuntScreen(audio: AudioManager, onComplete: () -> Unit, onBack: () -> Unit) {
    var roundIndex by remember { mutableStateOf(0) }
    var grid by remember(roundIndex) { mutableStateOf(buildGrid(huntTargets[roundIndex])) }
    var feedback by remember { mutableStateOf<AnswerFeedback>(AnswerFeedback.None) }
    var finished by remember { mutableStateOf(false) }

    val target = huntTargets[roundIndex]
    val foundCount = grid.count { it.isTarget && it.found }

    fun speakPrompt() = audio.speak("Find all the letter ${target}s hiding in the rice field.", rate = 0.9f)
    LaunchedEffect(roundIndex) { speakPrompt() }

    LaunchedEffect(feedback) {
        if (feedback !is AnswerFeedback.None) {
            delay(700)
            feedback = AnswerFeedback.None
        }
    }

    fun onCellTap(index: Int) {
        val cell = grid[index]
        if (cell.found) return
        if (cell.isTarget) {
            audio.playSfx(Sfx.CORRECT)
            grid = grid.toMutableList().also { it[index] = it[index].copy(found = true) }
            if (foundCount + 1 == TARGET_COUNT) {
                feedback = AnswerFeedback.Correct(Praise.randomCorrect())
            }
        } else {
            audio.playSfx(Sfx.TAP)
        }
    }

    LaunchedEffect(foundCount) {
        if (foundCount == TARGET_COUNT) {
            delay(1000)
            if (roundIndex == huntTargets.lastIndex) {
                audio.playSfx(Sfx.HARVEST)
                finished = true
            } else {
                roundIndex += 1
            }
        }
    }

    ActivityScaffold(
        title = "Letter Hunt",
        onBack = onBack,
        onReplayInstructions = { speakPrompt() },
        feedback = feedback
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Round ${roundIndex + 1} of ${huntTargets.size}", style = MaterialTheme.typography.bodyMedium)
            Text(
                "Find every \"$target\" ($foundCount / $TARGET_COUNT)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 12.dp)
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(8),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().weight(1f, fill = false)
            ) {
                items(grid.size) { i ->
                    val cell = grid[i]
                    LetterChip(
                        letter = cell.letter,
                        state = if (cell.found) ChoiceState.CORRECT else ChoiceState.IDLE,
                        enabled = !cell.found,
                        onClick = { onCellTap(i) }
                    )
                }
            }
        }

        if (finished) {
            ActivityCompleteOverlay(onContinue = onComplete)
        }
    }
}
