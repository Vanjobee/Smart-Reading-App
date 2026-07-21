package com.sgbread.app.screens.module1

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import com.sgbread.app.ui.components.ActivityCompleteOverlay
import com.sgbread.app.ui.components.ActivityScaffold
import com.sgbread.app.ui.components.AnswerFeedback
import com.sgbread.app.ui.components.ChoiceState
import com.sgbread.app.ui.components.LetterChip
import com.sgbread.app.ui.components.PictureChoiceCard
import kotlinx.coroutines.delay

private val basketTargets = LettersBank.phonicsItems.take(3) // A, B, C with farm-word icons

private fun freshPool(): List<Char> {
    val letters = basketTargets.flatMap { listOf(it.letter.uppercaseChar(), it.letter.lowercaseChar()) }
    return letters.shuffled()
}

@Composable
fun LetterBasketScreen(audio: AudioManager, onComplete: () -> Unit, onBack: () -> Unit) {
    var pool by remember { mutableStateOf(freshPool()) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var feedback by remember { mutableStateOf<AnswerFeedback>(AnswerFeedback.None) }
    var finished by remember { mutableStateOf(false) }

    fun instructions() = audio.speak("Drag or tap the correct letters into the matching basket.", rate = 0.9f)
    LaunchedEffect(Unit) { instructions() }

    LaunchedEffect(feedback) {
        if (feedback !is AnswerFeedback.None) {
            delay(900)
            feedback = AnswerFeedback.None
        }
    }

    fun onBasketTapped(target: Char) {
        val idx = selectedIndex ?: return
        val tile = pool.getOrNull(idx) ?: return
        if (tile.uppercaseChar() == target.uppercaseChar()) {
            audio.playSfx(Sfx.CORRECT)
            audio.speak(tile.toString())
            feedback = AnswerFeedback.Correct("Great Job!")
            pool = pool.toMutableList().also { it.removeAt(idx) }
            selectedIndex = null
            if (pool.isEmpty()) {
                audio.playSfx(Sfx.HARVEST)
                finished = true
            }
        } else {
            audio.playSfx(Sfx.INCORRECT)
            feedback = AnswerFeedback.Incorrect("Try another basket!")
            selectedIndex = null
        }
    }

    ActivityScaffold(
        title = "Letter Basket",
        onBack = onBack,
        onReplayInstructions = { instructions() },
        feedback = feedback
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Tap a letter, then tap its basket", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                basketTargets.forEach { target ->
                    PictureChoiceCard(
                        icon = target.icon,
                        label = target.letter.toString(),
                        onClick = { onBasketTapped(target.letter) }
                    )
                }
            }

            Text("Letters", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
                modifier = Modifier.fillMaxWidth().weight(1f, fill = false)
            ) {
                items(pool.size) { i ->
                    LetterChip(
                        letter = pool[i],
                        state = if (selectedIndex == i) ChoiceState.SELECTED else ChoiceState.IDLE,
                        onClick = {
                            selectedIndex = if (selectedIndex == i) null else i
                            audio.playSfx(Sfx.TAP)
                        }
                    )
                }
            }
        }

        if (finished) {
            ActivityCompleteOverlay(onContinue = onComplete)
        }
    }
}
