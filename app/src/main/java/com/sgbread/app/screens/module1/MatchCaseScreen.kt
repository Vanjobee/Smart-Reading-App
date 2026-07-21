package com.sgbread.app.screens.module1

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sgbread.app.audio.AudioManager
import com.sgbread.app.audio.Sfx
import com.sgbread.app.data.LettersBank
import com.sgbread.app.data.Praise
import com.sgbread.app.data.PhonicsItem
import com.sgbread.app.ui.components.ActivityCompleteOverlay
import com.sgbread.app.ui.components.ActivityScaffold
import com.sgbread.app.ui.components.AnswerFeedback
import com.sgbread.app.ui.components.ChoiceState
import com.sgbread.app.ui.components.LetterChip
import com.sgbread.app.ui.components.PictureChoiceCard
import com.sgbread.app.ui.theme.SunOrange
import kotlinx.coroutines.delay

private val matchTargets = LettersBank.phonicsItems.take(4) // A, B, C, D
private const val UPPER_PREFIX = "U:"
private const val LOWER_PREFIX = "L:"

/**
 * Match Upper & Lowercase: the child drags a line from a capital letter to
 * its lowercase pair (or vice versa) rather than tapping both, matching the
 * classic "draw a line to connect" workbook exercise.
 */
@Composable
fun MatchCaseScreen(audio: AudioManager, onComplete: () -> Unit, onBack: () -> Unit) {
    val uppers = remember { matchTargets.map { it.letter.uppercaseChar() }.shuffled() }
    val lowers = remember { matchTargets.map { it.letter.lowercaseChar() }.shuffled() }

    var matched by remember { mutableStateOf(setOf<Char>()) } // stores uppercase base letter
    var feedback by remember { mutableStateOf<AnswerFeedback>(AnswerFeedback.None) }
    var reveal by remember { mutableStateOf<PhonicsItem?>(null) }
    var finished by remember { mutableStateOf(false) }
    var lastWrongPair by remember { mutableStateOf<Pair<Char, Char>?>(null) }

    // Live drag-line state
    var dragFromKey by remember { mutableStateOf<String?>(null) }
    var dragCurrentPos by remember { mutableStateOf<Offset?>(null) }
    val chipPositions = remember { mutableMapOf<String, Offset>() }
    var containerCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    fun instructions() = audio.speak("Drag a line from each capital letter to its lowercase pair.", rate = 0.9f)
    LaunchedEffect(Unit) { instructions() }

    fun evaluate(upper: Char, lower: Char) {
        val base = upper.uppercaseChar()
        if (base == lower.uppercaseChar()) {
            audio.playSfx(Sfx.CORRECT)
            matched = matched + base
            val item = matchTargets.first { it.letter.uppercaseChar() == base }
            reveal = item
            audio.speak("${item.letter}. ${item.word}.")
            feedback = AnswerFeedback.Correct(Praise.randomCorrect())
        } else {
            audio.playSfx(Sfx.INCORRECT)
            lastWrongPair = upper to lower
            feedback = AnswerFeedback.Incorrect("Try again!")
        }
    }

    LaunchedEffect(feedback) {
        if (feedback !is AnswerFeedback.None) {
            delay(900)
            feedback = AnswerFeedback.None
            lastWrongPair = null
            if (matched.size == matchTargets.size) {
                audio.playSfx(Sfx.HARVEST)
                finished = true
            }
        }
    }

    LaunchedEffect(reveal) {
        if (reveal != null) {
            delay(1400)
            reveal = null
        }
    }

    ActivityScaffold(
        title = "Match Upper & Lowercase",
        onBack = onBack,
        onReplayInstructions = { instructions() },
        feedback = feedback
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Draw a line to connect each pair", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .onGloballyPositioned { containerCoords = it }
                    .pointerInput(matched) {
                        val hitRadius = 42.dp.toPx()

                        fun closestUnmatched(prefix: String, near: Offset): Map.Entry<String, Offset>? =
                            chipPositions.entries
                                .filter { (key, _) -> key.startsWith(prefix) && key.substring(2)[0].uppercaseChar() !in matched }
                                .minByOrNull { (_, pos) -> (pos - near).getDistance() }

                        detectDragGestures(
                            onDragStart = { offset ->
                                val nearest = chipPositions.entries
                                    .filter { (key, _) -> key.substring(2)[0].uppercaseChar() !in matched }
                                    .minByOrNull { (_, pos) -> (pos - offset).getDistance() }
                                if (nearest != null && (nearest.value - offset).getDistance() < hitRadius) {
                                    dragFromKey = nearest.key
                                    dragCurrentPos = offset
                                }
                            },
                            onDrag = { _, dragAmount ->
                                if (dragFromKey != null) {
                                    dragCurrentPos = (dragCurrentPos ?: Offset.Zero) + dragAmount
                                }
                            },
                            onDragEnd = {
                                val fromKey = dragFromKey
                                val endPos = dragCurrentPos
                                if (fromKey != null && endPos != null) {
                                    val fromIsUpper = fromKey.startsWith(UPPER_PREFIX)
                                    val targetPrefix = if (fromIsUpper) LOWER_PREFIX else UPPER_PREFIX
                                    val nearestTarget = closestUnmatched(targetPrefix, endPos)
                                    if (nearestTarget != null && (nearestTarget.value - endPos).getDistance() < hitRadius) {
                                        val fromLetter = fromKey.substring(2)[0]
                                        val toLetter = nearestTarget.key.substring(2)[0]
                                        if (fromIsUpper) evaluate(fromLetter, toLetter) else evaluate(toLetter, fromLetter)
                                    }
                                }
                                dragFromKey = null
                                dragCurrentPos = null
                            },
                            onDragCancel = {
                                dragFromKey = null
                                dragCurrentPos = null
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uppers.forEach { c ->
                            val base = c.uppercaseChar()
                            val isMatched = base in matched
                            val key = "$UPPER_PREFIX$c"
                            LetterChip(
                                letter = if (isMatched) ' ' else c,
                                state = when {
                                    isMatched -> ChoiceState.CORRECT
                                    dragFromKey == key -> ChoiceState.SELECTED
                                    lastWrongPair?.first == c -> ChoiceState.WRONG
                                    else -> ChoiceState.IDLE
                                },
                                enabled = !isMatched,
                                modifier = Modifier.onGloballyPositioned { coords ->
                                    val container = containerCoords ?: return@onGloballyPositioned
                                    val center = Offset(coords.size.width / 2f, coords.size.height / 2f)
                                    chipPositions[key] = container.localPositionOf(coords, center)
                                }
                            )
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        lowers.forEach { c ->
                            val base = c.uppercaseChar()
                            val isMatched = base in matched
                            val key = "$LOWER_PREFIX$c"
                            LetterChip(
                                letter = if (isMatched) ' ' else c,
                                state = when {
                                    isMatched -> ChoiceState.CORRECT
                                    dragFromKey == key -> ChoiceState.SELECTED
                                    lastWrongPair?.second == c -> ChoiceState.WRONG
                                    else -> ChoiceState.IDLE
                                },
                                enabled = !isMatched,
                                modifier = Modifier.onGloballyPositioned { coords ->
                                    val container = containerCoords ?: return@onGloballyPositioned
                                    val center = Offset(coords.size.width / 2f, coords.size.height / 2f)
                                    chipPositions[key] = container.localPositionOf(coords, center)
                                }
                            )
                        }
                    }
                }

                Canvas(modifier = Modifier.matchParentSize()) {
                    val from = dragFromKey?.let { chipPositions[it] }
                    val to = dragCurrentPos
                    if (from != null && to != null) {
                        drawLine(SunOrange, from, to, strokeWidth = 10f, cap = StrokeCap.Round)
                    }
                }
            }

            AnimatedVisibility(visible = reveal != null) {
                reveal?.let { item ->
                    Box(modifier = Modifier.padding(top = 24.dp)) {
                        PictureChoiceCard(icon = item.icon, label = item.word, onClick = {})
                    }
                }
            }
        }

        if (finished) {
            ActivityCompleteOverlay(onContinue = onComplete)
        }
    }
}
