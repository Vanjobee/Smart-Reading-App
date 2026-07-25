package com.sgbread.app.screens.module1

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.sgbread.app.ui.components.activityLayoutMetrics
import com.sgbread.app.ui.theme.SgbReadTheme
import com.sgbread.app.ui.theme.SunOrange
import kotlinx.coroutines.delay

private const val UPPER_PREFIX = "U:"
private const val LOWER_PREFIX = "L:"
private const val PAIRS_PER_ROUND = 2
private const val ROUND_COUNT = 5

/**
 * Match Upper & Lowercase: the child drags a line from a capital letter to
 * its lowercase pair (or vice versa) rather than tapping both, matching the
 * classic "draw a line to connect" workbook exercise. Split into rounds of
 * [PAIRS_PER_ROUND] pairs so the board never gets too crowded to scan.
 */
@Composable
fun MatchCaseScreen(audio: AudioManager, onComplete: () -> Unit, onBack: () -> Unit) {
    // Freshly shuffled each time the screen is entered, so replays don't always start on A-C.
    val rounds = remember { LettersBank.phonicsItems.shuffled().take(PAIRS_PER_ROUND * ROUND_COUNT).chunked(PAIRS_PER_ROUND) }
    var roundIndex by remember { mutableStateOf(0) }
    val matchTargets = rounds[roundIndex]
    val uppers = remember(roundIndex) { matchTargets.map { it.letter.uppercaseChar() }.shuffled() }
    val lowers = remember(roundIndex) { matchTargets.map { it.letter.lowercaseChar() }.shuffled() }
    val currentKeys = remember(roundIndex) {
        (uppers.map { "$UPPER_PREFIX$it" } + lowers.map { "$LOWER_PREFIX$it" }).toSet()
    }

    var matched by remember { mutableStateOf(setOf<Char>()) } // stores uppercase base letter, current round only
    var feedback by remember { mutableStateOf<AnswerFeedback>(AnswerFeedback.None) }
    var reveal by remember { mutableStateOf<PhonicsItem?>(null) }
    var finished by remember { mutableStateOf(false) }
    var lastWrongPair by remember { mutableStateOf<Pair<Char, Char>?>(null) }

    // Live drag-line state
    var dragFromKey by remember { mutableStateOf<String?>(null) }
    var dragCurrentPos by remember { mutableStateOf<Offset?>(null) }
    val chipPositions = remember { mutableMapOf<String, Offset>() }
    var containerCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    fun instructions() = audio.playRecordedPrompt(
        "Drag a line from each capital letter to its lowercase pair.",
        rate = 0.9f
    )
    LaunchedEffect(Unit) { instructions() }

    fun evaluate(upper: Char, lower: Char) {
        val base = upper.uppercaseChar()
        if (base == lower.uppercaseChar()) {
            audio.playSfx(Sfx.CORRECT)
            matched = matched + base
            val item = matchTargets.first { it.letter.uppercaseChar() == base }
            reveal = item
            audio.speakLetterNameThenWord(item.letter, item.word)
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
                if (roundIndex == rounds.size - 1) {
                    audio.playSfx(Sfx.HARVEST)
                    finished = true
                } else {
                    roundIndex += 1
                    matched = emptySet()
                }
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
        feedback = feedback,
        audio = audio
    ) { padding ->
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val metrics = activityLayoutMetrics(maxWidth, maxHeight)
            // Scale letters up well beyond the shared chip size for readability, but bound
            // by how much height PAIRS_PER_ROUND stacked chips can actually claim -- so the
            // enlargement never clips or pushes the last pair off a short landscape screen.
            val reservedForHeader = if (metrics.compactHeight) 60.dp else 80.dp
            val availableForChips = maxHeight - reservedForHeader
            val maxChipHeight = ((availableForChips - metrics.spacing * (PAIRS_PER_ROUND - 1)) / PAIRS_PER_ROUND)
                .coerceAtLeast(metrics.chipSize)
            val bigChipSize = (metrics.chipSize * 1.7f).coerceIn(metrics.chipSize, maxChipHeight)
            val bigFontSize = (bigChipSize.value * 0.48f).sp
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Draw a line to connect each pair", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
                Text(
                    "Round ${roundIndex + 1} of ${rounds.size}",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = 4.dp)
                        .onGloballyPositioned { containerCoords = it }
                        .pointerInput(roundIndex, matched, bigChipSize) {
                            val hitRadius = maxOf(42.dp.toPx(), bigChipSize.toPx() * 0.8f)

                        // Restrict to the current round's keys: stale positions from earlier
                        // rounds would otherwise be eligible and can "win" the nearest check.
                        fun closestUnmatched(prefix: String, near: Offset): Map.Entry<String, Offset>? =
                            chipPositions.entries
                                .filter { (key, _) -> key.startsWith(prefix) && key in currentKeys && key.substring(2)[0].uppercaseChar() !in matched }
                                .minByOrNull { (_, pos) -> (pos - near).getDistance() }

                        detectDragGestures(
                            onDragStart = { offset ->
                                val nearest = chipPositions.entries
                                    .filter { (key, _) -> key in currentKeys && key.substring(2)[0].uppercaseChar() !in matched }
                                    .minByOrNull { (_, pos) -> (pos - offset).getDistance() }
                                if (nearest != null && (nearest.value - offset).getDistance() < hitRadius) {
                                    dragFromKey = nearest.key
                                    dragCurrentPos = offset
                                    audio.playLetterName(nearest.key.substring(2)[0])
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
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(
                            modifier = Modifier.fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            uppers.forEach { c ->
                                val base = c.uppercaseChar()
                                val isMatched = base in matched
                                val key = "$UPPER_PREFIX$c"
                                LetterChip(
                                    letter = if (isMatched) ' ' else c,
                                    size = bigChipSize,
                                    fontSize = bigFontSize,
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
                        Column(
                            modifier = Modifier.fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            lowers.forEach { c ->
                                val base = c.uppercaseChar()
                                val isMatched = base in matched
                                val key = "$LOWER_PREFIX$c"
                                LetterChip(
                                    letter = if (isMatched) ' ' else c,
                                    size = bigChipSize,
                                    fontSize = bigFontSize,
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
            }

            // Overlaid rather than laid out in-flow, so the brief reveal popup never
            // competes with the letter columns for the column's height budget.
            AnimatedVisibility(
                visible = reveal != null,
                modifier = Modifier.align(Alignment.Center)
            ) {
                reveal?.let { item ->
                    PictureChoiceCard(image = item.image, label = item.word, imageSize = metrics.choiceImageSize, onClick = {})
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
private fun MatchCaseScreenPreview() {
    SgbReadTheme {
        MatchCaseScreen(audio = AudioManager.getInstance(LocalContext.current), onComplete = {}, onBack = {})
    }
}
