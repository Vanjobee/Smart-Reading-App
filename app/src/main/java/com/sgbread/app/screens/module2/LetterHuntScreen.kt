package com.sgbread.app.screens.module2

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
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
import com.sgbread.app.ui.components.activityLayoutMetrics
import com.sgbread.app.ui.theme.SgbReadTheme
import kotlinx.coroutines.delay
import kotlin.random.Random

private data class HuntCell(val letter: Char, val isTarget: Boolean, var found: Boolean = false)

// At most this many letters appear on screen at once; the child must clear TARGET_COUNT
// of them (the rest are distractors) to pass the round.
private const val ON_SCREEN_COUNT = 7
private const val TARGET_COUNT = 5
private const val M2_SCALE = 1.25f

private fun buildLetters(target: Char): List<HuntCell> {
    val distractors = ALPHABET.filter { it != target }
    val targetCells = List(TARGET_COUNT) { HuntCell(target, true) }
    val fillerCells = List(ON_SCREEN_COUNT - TARGET_COUNT) { HuntCell(distractors.random(), false) }
    return (targetCells + fillerCells).shuffled()
}

/** Random, non-overlapping positions (top-left corners, in px) for [count] chips of
 * [chipPx] within a [boundsWidthPx] x [boundsHeightPx] area. Rejection-sampled against
 * already-placed chips so letters never stack on top of each other or clip off-screen;
 * falls back to whatever the last attempt found if a spot never opens up. */
private fun scatterPositions(
    count: Int,
    boundsWidthPx: Float,
    boundsHeightPx: Float,
    chipPx: Float,
    seed: Int
): List<Offset> {
    val random = Random(seed)
    val maxX = (boundsWidthPx - chipPx).coerceAtLeast(0f)
    val maxY = (boundsHeightPx - chipPx).coerceAtLeast(0f)
    val minDistance = chipPx * 1.15f
    val positions = mutableListOf<Offset>()
    repeat(count) {
        var candidate = Offset(random.nextFloat() * maxX, random.nextFloat() * maxY)
        var attempts = 0
        while (attempts < 40 && positions.any { (it - candidate).getDistance() < minDistance }) {
            candidate = Offset(random.nextFloat() * maxX, random.nextFloat() * maxY)
            attempts++
        }
        positions.add(candidate)
    }
    return positions
}

@Composable
fun LetterHuntScreen(audio: AudioManager, onComplete: () -> Unit, onBack: () -> Unit) {
    // Freshly shuffled each time the screen is entered, not just once per app launch.
    val huntRounds = remember { LettersBank.phonicsItems.shuffled().take(10) }
    var roundIndex by remember { mutableStateOf(0) }
    var letters by remember(roundIndex) { mutableStateOf(buildLetters(huntRounds[roundIndex].letter)) }
    var feedback by remember { mutableStateOf<AnswerFeedback>(AnswerFeedback.None) }
    var pendingPraise by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }

    val round = huntRounds[roundIndex]
    val target = round.letter
    val foundCount = letters.count { it.isTarget && it.found }

    // Phonics sound then the word, in sequence, so the child hunts by sound rather than by name.
    fun speakPrompt() = audio.speakLetterThenWord(target, round.word, rate = 0.9f)

    var hasIntroduced by remember { mutableStateOf(false) }
    LaunchedEffect(roundIndex) {
        if (!hasIntroduced) {
            hasIntroduced = true
            audio.playRecordedPrompt("Lets search the letter in the farm!")
            delay(900)
        }
        speakPrompt()
    }

    fun onCellTap(index: Int) {
        val cell = letters[index]
        if (cell.found) return
        if (cell.isTarget) {
            audio.playSfx(Sfx.CORRECT)
            letters = letters.toMutableList().also { it[index] = it[index].copy(found = true) }
            if (foundCount + 1 == TARGET_COUNT) {
                audio.speakLetterThenWord(target, round.word) // "A is for ant"
                pendingPraise = true
            }
        } else {
            audio.playSfx(Sfx.TAP)
        }
    }

    LaunchedEffect(pendingPraise) {
        if (pendingPraise) {
            delay(1500)
            audio.playSfx(Sfx.CORRECT)
            feedback = AnswerFeedback.Correct(Praise.randomCorrect())
            pendingPraise = false
        }
    }

    LaunchedEffect(feedback) {
        if (feedback is AnswerFeedback.Correct) {
            delay(1200)
            feedback = AnswerFeedback.None
            if (roundIndex == huntRounds.lastIndex) {
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
        feedback = feedback,
        audio = audio
    ) { padding ->
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val metrics = activityLayoutMetrics(maxWidth, maxHeight)
            val chipSize = metrics.chipSize * M2_SCALE
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = metrics.horizontalPadding, vertical = metrics.verticalPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Round ${roundIndex + 1} of ${huntRounds.size}",
                    style = if (metrics.compactHeight) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge
                )
                Text(
                    "Find every \"$target\" ($foundCount / $TARGET_COUNT)",
                    style = if (metrics.compactHeight) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = metrics.spacing)
                )
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .fillMaxSize()
                ) {
                    val density = LocalDensity.current
                    val chipPx = with(density) { chipSize.toPx() }
                    val widthPx = with(density) { maxWidth.toPx() }
                    val heightPx = with(density) { maxHeight.toPx() }
                    val positions = remember(roundIndex, widthPx, heightPx, chipPx) {
                        scatterPositions(letters.size, widthPx, heightPx, chipPx, seed = roundIndex)
                    }
                    letters.forEachIndexed { i, cell ->
                        val pos = positions.getOrElse(i) { Offset.Zero }
                        val offsetX = with(density) { pos.x.toDp() }
                        val offsetY = with(density) { pos.y.toDp() }
                        LetterChip(
                            letter = cell.letter,
                            size = chipSize,
                            state = if (cell.found) ChoiceState.CORRECT else ChoiceState.IDLE,
                            enabled = !cell.found,
                            onClick = { onCellTap(i) },
                            modifier = Modifier.offset(x = offsetX, y = offsetY)
                        )
                    }
                }
            }
        }

        if (finished) {
            ActivityCompleteOverlay(onContinue = onComplete)
        }
    }
}

@Preview(device = "spec:width=360dp,height=800dp,orientation=portrait", showBackground = true)
@Composable
private fun LetterHuntScreenPreview() {
    SgbReadTheme {
        LetterHuntScreen(audio = AudioManager.getInstance(LocalContext.current), onComplete = {}, onBack = {})
    }
}
