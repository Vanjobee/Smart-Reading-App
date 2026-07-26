package com.sgbread.app.screens.module2

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
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
import com.sgbread.app.ui.theme.CreamWhite
import com.sgbread.app.ui.theme.SgbReadTheme
import kotlinx.coroutines.delay
import kotlin.math.ceil
import kotlin.math.sqrt
import kotlin.random.Random

private data class HuntCell(val letter: Char, val isTarget: Boolean, var found: Boolean = false)

// At most this many letters appear on screen at once; the child must clear TARGET_COUNT
// of them (the rest are distractors) to pass the round.
private const val ON_SCREEN_COUNT = 20
private const val TARGET_COUNT = 8
private const val M2_SCALE = 0.86f
private val PHONICS_HUNT_EXCLUDED_WORDS = setOf("goat", "rice")

private fun buildLetters(target: Char): List<HuntCell> {
    val distractors = ALPHABET.filter { it != target }
    val targetCells = List(TARGET_COUNT) { index ->
        HuntCell(
            letter = if (index % 2 == 0) target.uppercaseChar() else target.lowercaseChar(),
            isTarget = true
        )
    }
    val fillerCells = List(ON_SCREEN_COUNT - TARGET_COUNT) {
        val letter = distractors.random()
        HuntCell(
            letter = if (Random.nextBoolean()) letter.uppercaseChar() else letter.lowercaseChar(),
            isTarget = false
        )
    }
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
    if (count <= 0 || boundsWidthPx <= 0f || boundsHeightPx <= 0f) return emptyList()

    val aspect = boundsWidthPx / boundsHeightPx.coerceAtLeast(1f)
    val columns = ceil(sqrt(count * aspect)).toInt().coerceAtLeast(1)
    val rows = ceil(count / columns.toFloat()).toInt().coerceAtLeast(1)
    val edgePadding = chipPx * 0.16f
    val usableWidth = (boundsWidthPx - chipPx - edgePadding * 2f).coerceAtLeast(0f)
    val usableHeight = (boundsHeightPx - chipPx - edgePadding * 2f).coerceAtLeast(0f)
    val cellWidth = if (columns > 1) usableWidth / (columns - 1) else 0f
    val cellHeight = if (rows > 1) usableHeight / (rows - 1) else 0f
    val jitterX = minOf(cellWidth, chipPx) * 0.22f
    val jitterY = minOf(cellHeight, chipPx) * 0.22f

    return List(count) { index ->
        val row = index / columns
        val col = index % columns
        val baseX = edgePadding + col * cellWidth
        val baseY = edgePadding + row * cellHeight
        val offsetX = if (columns > 1) (random.nextFloat() - 0.5f) * jitterX else 0f
        val offsetY = if (rows > 1) (random.nextFloat() - 0.5f) * jitterY else 0f
        Offset(
            x = (baseX + offsetX).coerceIn(0f, (boundsWidthPx - chipPx).coerceAtLeast(0f)),
            y = (baseY + offsetY).coerceIn(0f, (boundsHeightPx - chipPx).coerceAtLeast(0f))
        )
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun LetterHuntScreen(audio: AudioManager?, onComplete: () -> Unit, onBack: () -> Unit) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val responsiveTextScale = (screenWidthDp / 360f).coerceIn(1f, 1.25f)

    // Freshly shuffled each time the screen is entered, not just once per app launch.
    val huntRounds = remember {
        LettersBank.phonicsItems.filter { it.word !in PHONICS_HUNT_EXCLUDED_WORDS }.shuffled().take(10)
    }
    var roundIndex by remember { mutableStateOf(0) }
    var letters by remember(roundIndex) { mutableStateOf(buildLetters(huntRounds[roundIndex].letter)) }
    var feedback by remember { mutableStateOf<AnswerFeedback>(AnswerFeedback.None) }
    var pendingPraise by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }

    val round = huntRounds[roundIndex]
    val target = round.letter
    val targetLabel = "${target.uppercaseChar()}/${target.lowercaseChar()}"
    val foundCount = letters.count { it.isTarget && it.found }

    // Phonics sound then the word, in sequence, so the child hunts by sound rather than by name.
    fun speakPrompt() = audio?.speakLetterThenWord(target, round.word, rate = 0.9f)

    var hasIntroduced by remember { mutableStateOf(false) }
    LaunchedEffect(roundIndex) {
        if (!hasIntroduced) {
            hasIntroduced = true
            audio?.playRecordedPrompt("Lets search the letter in the farm!") {
                speakPrompt()
            }
            return@LaunchedEffect
        }
        speakPrompt()
    }

    fun onCellTap(index: Int) {
        val cell = letters[index]
        if (cell.found) return
        audio?.stopPlayback()
        if (cell.isTarget) {
            audio?.playSfx(Sfx.CORRECT)
            letters = letters.toMutableList().also { it[index] = it[index].copy(found = true) }
            if (foundCount + 1 == TARGET_COUNT) {
                audio?.speakLetterThenWord(target, round.word) {
                    pendingPraise = true
                }
            } else {
                audio?.playLetterSound(cell.letter)
            }
        } else {
            audio?.playLetterSound(cell.letter)
        }
    }

    LaunchedEffect(pendingPraise) {
        if (pendingPraise) {
            delay(300)
            audio?.playSfx(Sfx.CORRECT)
            feedback = AnswerFeedback.Correct(Praise.randomCorrect())
            pendingPraise = false
        }
    }

    LaunchedEffect(feedback) {
        if (feedback is AnswerFeedback.Correct) {
            delay(850)
            while (audio?.isPlaying?.value == true) delay(100)
            feedback = AnswerFeedback.None
            if (roundIndex == huntRounds.lastIndex) {
                audio?.playSfx(Sfx.HARVEST)
                finished = true
            } else {
                roundIndex += 1
            }
        }
    }

    ActivityScaffold(
        title = "Phonics Hunt",
        onBack = onBack,
        onReplayInstructions = { speakPrompt() },
        feedback = feedback,
        audio = audio,
        blockInputDuringAudio = false,
        titleTextScale = responsiveTextScale,
        confirmOnBack = roundIndex > 0 || foundCount > 0
    ) { padding ->
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val metrics = activityLayoutMetrics(maxWidth, maxHeight)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.42f))
            )
            val chipSize = if (metrics.compactHeight || metrics.compactWidth) {
                metrics.chipSize * 0.78f
            } else {
                metrics.chipSize * M2_SCALE
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(
                        horizontal = metrics.horizontalPadding,
                        vertical = if (metrics.compactHeight) 2.dp else metrics.verticalPadding
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Round ${roundIndex + 1} of ${huntRounds.size}",
                    style = (
                        if (metrics.compactHeight) {
                            MaterialTheme.typography.labelLarge
                        } else {
                            MaterialTheme.typography.titleLarge
                        }
                    ).let { baseStyle ->
                        baseStyle.copy(
                            color = CreamWhite,
                            fontSize = baseStyle.fontSize * responsiveTextScale
                        )
                    }
                )
                Text(
                    "Find every \"$targetLabel\" sound ($foundCount / $TARGET_COUNT)",
                    style = (
                        if (metrics.compactHeight) {
                            MaterialTheme.typography.titleLarge
                        } else {
                            MaterialTheme.typography.headlineMedium
                        }
                    ).let { baseStyle ->
                        baseStyle.copy(
                            color = CreamWhite,
                            fontSize = baseStyle.fontSize * responsiveTextScale
                        )
                    },
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = if (metrics.compactHeight) 2.dp else metrics.spacing)
                )
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = true)
                        .background(CreamWhite.copy(alpha = 0.28f), RoundedCornerShape(24.dp))
                        .padding(if (metrics.compactHeight) 6.dp else 10.dp)
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
        LetterHuntScreen(audio = null, onComplete = {}, onBack = {})
    }
}
