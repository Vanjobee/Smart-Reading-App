package com.sgbread.app.screens.shared

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
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
import com.sgbread.app.ui.components.ResponsiveColumn
import com.sgbread.app.ui.components.TwoPaneActivityBody
import com.sgbread.app.ui.components.activityLayoutMetrics
import com.sgbread.app.ui.icons.FarmIcon
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

data class BuildableWord(val word: String, val icon: FarmIconKey? = null, val image: Int? = null)

private data class Tile(val letter: Char, var placedAt: Int? = null)

private fun buildTiles(word: String): List<Tile> {
    val extras = ('a'..'z').filter { it !in word.lowercase() }.shuffled().take(2)
    return (word.toList() + extras).shuffled().map { Tile(it) }
}

private const val DROP_HIT_RADIUS_DP = 44

/**
 * Shared "build the word from letter tiles" mechanic used by the Blending
 * module (CVC words) and the Digraph module (build a digraph word). Letter
 * tiles are dragged out of the pool and into the word's blank slots.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordBuilderScreen(
    title: String,
    instructionVerb: String,
    words: List<BuildableWord>,
    audio: AudioManager,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    val rounds = remember { words.shuffled().take(10) }
    var roundIndex by remember { mutableStateOf(0) }
    var tiles by remember(roundIndex) { mutableStateOf(buildTiles(rounds[roundIndex].word)) }
    var feedback by remember { mutableStateOf<AnswerFeedback>(AnswerFeedback.None) }
    var pendingPraise by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }

    var draggingTileIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var hoveredSlot by remember { mutableStateOf<Int?>(null) }
    val chipPositions = remember(roundIndex) { mutableMapOf<Int, Offset>() }
    val slotPositions = remember(roundIndex) { mutableMapOf<Int, Offset>() }
    var containerCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    val round = rounds[roundIndex]
    val slots = round.word.length

    fun speakPrompt() = audio.playWord(round.word, rate = 0.85f)

    var hasIntroduced by remember { mutableStateOf(false) }
    LaunchedEffect(roundIndex) {
        if (!hasIntroduced) {
            hasIntroduced = true
            audio.playRecordedPrompt("Put the letters in the correct order!") {
                speakPrompt()
            }
            return@LaunchedEffect
        }
        speakPrompt()
    }

    fun checkAnswer() {
        val answer = (0 until slots).map { slotIndex -> tiles.first { it.placedAt == slotIndex }.letter }.joinToString("")
        if (answer.equals(round.word, ignoreCase = true)) {
            if (roundIndex == rounds.lastIndex) {
                finished = true
                feedback = AnswerFeedback.Correct("Great job!")
                audio.playWordThenRecorded(round.word, "Great job!")
            } else {
                audio.playWord(round.word, rate = 0.9f) {
                    pendingPraise = true
                }
            }
        } else {
            audio.playSfx(Sfx.INCORRECT)
            feedback = AnswerFeedback.Incorrect(Praise.randomEncouragement())
        }
    }

    LaunchedEffect(pendingPraise) {
        if (pendingPraise) {
            delay(250)
            audio.playSfx(Sfx.CORRECT)
            feedback = AnswerFeedback.Correct(Praise.randomCorrect())
            pendingPraise = false
        }
    }

    LaunchedEffect(feedback) {
        val current = feedback
        if (current is AnswerFeedback.Correct) {
            delay(850)
            while (audio.isPlaying.value) delay(100)
            feedback = AnswerFeedback.None
            if (!finished) {
                roundIndex += 1
            }
        } else if (current is AnswerFeedback.Incorrect) {
            delay(750)
            feedback = AnswerFeedback.None
            tiles = tiles.map { it.copy(placedAt = null) }
        }
    }

    fun onSlotTap(slot: Int) {
        val placedIndex = tiles.indexOfFirst { it.placedAt == slot }
        if (placedIndex != -1) {
            audio.stopPlayback()
            tiles = tiles.toMutableList().also { it[placedIndex] = it[placedIndex].copy(placedAt = null) }
            audio.playSfx(Sfx.TAP)
        }
    }

    ActivityScaffold(
        title = title,
        onBack = onBack,
        onReplayInstructions = { speakPrompt() },
        feedback = feedback,
        audio = audio,
        playFeedbackAudio = !finished,
        blockInputDuringAudio = false
    ) { padding ->
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val metrics = activityLayoutMetrics(maxWidth, maxHeight)
            ResponsiveColumn(
                metrics = metrics,
                modifier = Modifier.padding(padding),
                verticalArrangement = Arrangement.spacedBy(if (metrics.compactHeight) 2.dp else metrics.spacing),
                verticalPadding = if (metrics.compactHeight) 2.dp else metrics.verticalPadding
            ) {
                Text(
                    "Word ${roundIndex + 1} of ${rounds.size}",
                    style = if (metrics.compactHeight) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodyMedium,
                )

                // The drag interaction is kept in a fixed-size container to avoid
                // scrolling conflicts while the child is dragging tiles.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(if (metrics.compactHeight) 280.dp else 460.dp)
                        .onGloballyPositioned { containerCoords = it }
                        .pointerInput(roundIndex, metrics.chipSize) {
                            val hitRadius = maxOf(DROP_HIT_RADIUS_DP.dp.toPx(), metrics.chipSize.toPx() * 0.85f)
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val nearest = chipPositions.entries.minByOrNull { (_, pos) -> (pos - offset).getDistance() }
                                    if (nearest != null && (nearest.value - offset).getDistance() < hitRadius) {
                                        audio.stopPlayback()
                                        draggingTileIndex = nearest.key
                                        dragOffset = Offset.Zero
                                        audio.playLetterSound(tiles[nearest.key].letter)
                                    }
                                },
                                onDrag = { _, dragAmount ->
                                    val idx = draggingTileIndex
                                    if (idx != null) {
                                        dragOffset += dragAmount
                                        val start = chipPositions[idx]
                                        if (start != null) {
                                            val current = start + dragOffset
                                            hoveredSlot = slotPositions.entries
                                                .filter { (slot, _) -> tiles.none { it.placedAt == slot } }
                                                .minByOrNull { (_, pos) -> (pos - current).getDistance() }
                                                ?.takeIf { (it.value - current).getDistance() < hitRadius }
                                                ?.key
                                        }
                                    }
                                },
                                onDragEnd = {
                                    val idx = draggingTileIndex
                                    val slot = hoveredSlot
                                    if (idx != null && slot != null) {
                                        tiles = tiles.toMutableList().also { it[idx] = it[idx].copy(placedAt = slot) }
                                        audio.playLetterSound(tiles[idx].letter)
                                        if (tiles.count { it.placedAt != null } == slots) checkAnswer()
                                    }
                                    draggingTileIndex = null
                                    dragOffset = Offset.Zero
                                    hoveredSlot = null
                                },
                                onDragCancel = {
                                    draggingTileIndex = null
                                    dragOffset = Offset.Zero
                                    hoveredSlot = null
                                }
                            )
                        }
                ) {
                    TwoPaneActivityBody(
                        metrics = metrics,
                        question = {
                            if (round.image != null) {
                                Image(
                                    painter = painterResource(round.image),
                                    contentDescription = round.word,
                                    modifier = Modifier
                                        .size(metrics.largePictureSize)
                                        .padding(bottom = metrics.spacing),
                                    contentScale = ContentScale.Fit
                                )
                            } else if (round.icon != null) {
                                FarmIcon(
                                    round.icon,
                                    modifier = Modifier.size(metrics.largePictureSize).padding(bottom = metrics.spacing)
                                )
                            }
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalArrangement = Arrangement.spacedBy(metrics.gridSpacing)
                            ) {
                                for (slot in 0 until slots) {
                                    val filledLetter = tiles.firstOrNull { it.placedAt == slot }?.letter
                                    LetterChip(
                                        letter = filledLetter ?: ' ',
                                        size = metrics.chipSize,
                                        state = if (hoveredSlot == slot) ChoiceState.SELECTED else ChoiceState.IDLE,
                                        onClick = { onSlotTap(slot) },
                                        modifier = Modifier
                                            .padding(3.dp)
                                            .onGloballyPositioned { coords ->
                                                val container = containerCoords ?: return@onGloballyPositioned
                                                val center = Offset(coords.size.width / 2f, coords.size.height / 2f)
                                                slotPositions[slot] = container.localPositionOf(coords, center)
                                            }
                                    )
                                }
                            }

                        },
                        choices = {
                            Text(
                                "Drag the letters in order",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = metrics.spacing)
                            )
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(metrics.gridSpacing, Alignment.CenterHorizontally),
                                verticalArrangement = Arrangement.spacedBy(metrics.gridSpacing)
                            ) {
                                tiles.forEachIndexed { i, tile ->
                                    if (tile.placedAt != null) return@forEachIndexed
                                    val isDragging = draggingTileIndex == i
                                    LetterChip(
                                        letter = tile.letter,
                                        size = metrics.chipSize,
                                        state = if (isDragging) ChoiceState.SELECTED else ChoiceState.IDLE,
                                        modifier = Modifier
                                            .onGloballyPositioned { coords ->
                                                if (i == draggingTileIndex) return@onGloballyPositioned
                                                val container = containerCoords ?: return@onGloballyPositioned
                                                val center = Offset(coords.size.width / 2f, coords.size.height / 2f)
                                                chipPositions[i] = container.localPositionOf(coords, center)
                                            }
                                            .then(
                                                if (isDragging) {
                                                    Modifier.offset { IntOffset(dragOffset.x.roundToInt(), dragOffset.y.roundToInt()) }
                                                } else {
                                                    Modifier
                                                }
                                            )
                                    )
                                }
                            }
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
