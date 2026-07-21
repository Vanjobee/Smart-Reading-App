package com.sgbread.app.screens.shared

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.sgbread.app.data.FarmIconKey
import com.sgbread.app.data.Praise
import com.sgbread.app.ui.components.ActivityCompleteOverlay
import com.sgbread.app.ui.components.ActivityScaffold
import com.sgbread.app.ui.components.AnswerFeedback
import com.sgbread.app.ui.components.ChoiceState
import com.sgbread.app.ui.components.LetterChip
import com.sgbread.app.ui.icons.FarmIcon
import kotlinx.coroutines.delay

data class BuildableWord(val word: String, val icon: FarmIconKey)

private data class Tile(val letter: Char, var placedAt: Int? = null)

private fun buildTiles(word: String): List<Tile> {
    val extras = ('a'..'z').filter { it !in word.lowercase() }.shuffled().take(2)
    return (word.toList() + extras).shuffled().map { Tile(it) }
}

/**
 * Shared "build the word from letter tiles" mechanic used by the Blending
 * module (CVC words) and the Digraph module (build a digraph word).
 */
@Composable
fun WordBuilderScreen(
    title: String,
    instructionVerb: String,
    words: List<BuildableWord>,
    audio: AudioManager,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    val rounds = remember { words.shuffled().take(5) }
    var roundIndex by remember { mutableStateOf(0) }
    var tiles by remember(roundIndex) { mutableStateOf(buildTiles(rounds[roundIndex].word)) }
    var feedback by remember { mutableStateOf<AnswerFeedback>(AnswerFeedback.None) }
    var finished by remember { mutableStateOf(false) }

    val round = rounds[roundIndex]
    val slots = round.word.length

    fun speakPrompt() = audio.speak("$instructionVerb ${round.word}.", rate = 0.85f)
    LaunchedEffect(roundIndex) { speakPrompt() }

    fun checkAnswer() {
        val answer = (0 until slots).map { slotIndex -> tiles.first { it.placedAt == slotIndex }.letter }.joinToString("")
        if (answer.equals(round.word, ignoreCase = true)) {
            audio.playSfx(Sfx.CORRECT)
            audio.speak(round.word)
            feedback = AnswerFeedback.Correct(Praise.randomCorrect())
        } else {
            audio.playSfx(Sfx.INCORRECT)
            feedback = AnswerFeedback.Incorrect(Praise.randomEncouragement())
        }
    }

    LaunchedEffect(feedback) {
        val current = feedback
        if (current is AnswerFeedback.Correct) {
            delay(1200)
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
            tiles = tiles.map { it.copy(placedAt = null) }
        }
    }

    fun onTileTap(tileIndex: Int) {
        val tile = tiles[tileIndex]
        val updated = tiles.toMutableList()
        if (tile.placedAt != null) {
            updated[tileIndex] = tile.copy(placedAt = null)
        } else {
            val nextSlot = (0 until slots).firstOrNull { slot -> updated.none { it.placedAt == slot } }
            if (nextSlot != null) {
                updated[tileIndex] = tile.copy(placedAt = nextSlot)
            }
        }
        tiles = updated
        audio.playSfx(Sfx.TAP)
        if (updated.count { it.placedAt != null } == slots) checkAnswer()
    }

    ActivityScaffold(
        title = title,
        onBack = onBack,
        onReplayInstructions = { speakPrompt() },
        feedback = feedback
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(padding).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Word ${roundIndex + 1} of ${rounds.size}", style = MaterialTheme.typography.bodyMedium)
            FarmIcon(round.icon, modifier = Modifier.size(90.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                for (slot in 0 until slots) {
                    val filledLetter = tiles.firstOrNull { it.placedAt == slot }?.letter
                    LetterChip(
                        letter = filledLetter ?: ' ',
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }

            Text("Tap the letters in order", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                tiles.forEachIndexed { i, tile ->
                    LetterChip(
                        letter = tile.letter,
                        state = if (tile.placedAt != null) ChoiceState.SELECTED else ChoiceState.IDLE,
                        onClick = { onTileTap(i) }
                    )
                }
            }
        }

        if (finished) {
            ActivityCompleteOverlay(onContinue = onComplete)
        }
    }
}
