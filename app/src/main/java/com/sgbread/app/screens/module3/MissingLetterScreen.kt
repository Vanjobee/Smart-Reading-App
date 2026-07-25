package com.sgbread.app.screens.module3

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
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
import com.sgbread.app.data.LettersBank
import com.sgbread.app.data.Praise
import com.sgbread.app.ui.components.ActivityCompleteOverlay
import com.sgbread.app.ui.components.ActivityScaffold
import com.sgbread.app.ui.components.AnswerFeedback
import com.sgbread.app.ui.components.ChoiceState
import com.sgbread.app.ui.components.LetterChip
import com.sgbread.app.ui.components.TwoPaneActivityBody
import com.sgbread.app.ui.components.activityLayoutMetrics
import com.sgbread.app.ui.icons.FarmIcon
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private const val DROP_HIT_RADIUS_DP = 44
private const val CHOICE_COUNT = 4

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MissingLetterScreen(audio: AudioManager, onComplete: () -> Unit, onBack: () -> Unit) {
    // Freshly shuffled each time the screen is entered, not just once per app launch.
    val rounds = remember { LettersBank.blendWords.filter { it.word.length == 3 }.shuffled().take(10) }
    var roundIndex by remember { mutableStateOf(0) }
    var feedback by remember { mutableStateOf<AnswerFeedback>(AnswerFeedback.None) }
    var wrongLetter by remember { mutableStateOf<Char?>(null) }
    var filledLetter by remember { mutableStateOf<Char?>(null) }
    var finished by remember { mutableStateOf(false) }

    var draggingLetter by remember { mutableStateOf<Char?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var isHoveringBlank by remember { mutableStateOf(false) }
    val chipPositions = remember(roundIndex) { mutableMapOf<Char, Offset>() }
    var blankCenter by remember { mutableStateOf<Offset?>(null) }
    var blankRadius by remember { mutableStateOf(0f) }
    var containerCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    val round = rounds[roundIndex]
    val blankIndex = remember(roundIndex) { listOf(0, round.word.lastIndex).random() }
    val missingLetter = round.word[blankIndex]
    val choices = remember(roundIndex) {
        val distractors = ('a'..'z')
            .filter { it != missingLetter }
            .shuffled()
            .take(CHOICE_COUNT - 1)
        (distractors + missingLetter).distinct().shuffled()
    }

    fun speakPrompt() = audio.playWord(round.word, rate = 0.85f)

    var hasIntroduced by remember { mutableStateOf(false) }
    LaunchedEffect(roundIndex) {
        wrongLetter = null
        filledLetter = null
        if (!hasIntroduced) {
            hasIntroduced = true
            audio.playRecordedPrompt("Which letter is missing?")
            delay(900)
        }
        speakPrompt()
    }

    fun onPick(letter: Char) {
        if (letter == missingLetter) {
            filledLetter = letter
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
            while (audio.isPlaying.value) delay(100)
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
        feedback = feedback,
        audio = audio,
        blockInputDuringAudio = false
    ) { padding ->
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val metrics = activityLayoutMetrics(maxWidth, maxHeight)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = metrics.horizontalPadding, vertical = metrics.verticalPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Word ${roundIndex + 1} of ${rounds.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = metrics.spacing)
                )

                // Wraps both panes so a letter can be dragged from the choices (right)
                // across into the blank (left); the gesture and position tracking need
                // one shared coordinate space regardless of which pane a chip sits in.
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .onGloballyPositioned { containerCoords = it }
                        .pointerInput(roundIndex, metrics.chipSize) {
                            val hitRadius = maxOf(DROP_HIT_RADIUS_DP.dp.toPx(), metrics.chipSize.toPx() * 0.85f)
                            detectDragGestures(
                                onDragStart = { offset ->
                                    if (filledLetter != null) return@detectDragGestures
                                    val nearest = chipPositions.entries.minByOrNull { (_, pos) -> (pos - offset).getDistance() }
                                    if (nearest != null && (nearest.value - offset).getDistance() < hitRadius) {
                                        audio.stopPlayback()
                                        draggingLetter = nearest.key
                                        dragOffset = Offset.Zero
                                        audio.playLetterSound(nearest.key)
                                    }
                                },
                                onDrag = { _, dragAmount ->
                                    if (draggingLetter != null) {
                                        dragOffset += dragAmount
                                        val start = chipPositions[draggingLetter]
                                        val center = blankCenter
                                        if (start != null && center != null) {
                                            isHoveringBlank = (start + dragOffset - center).getDistance() < blankRadius
                                        }
                                    }
                                },
                                onDragEnd = {
                                    val letter = draggingLetter
                                    val start = letter?.let { chipPositions[it] }
                                    val center = blankCenter
                                    isHoveringBlank = false
                                    if (letter != null && start != null && center != null &&
                                        (start + dragOffset - center).getDistance() < blankRadius
                                    ) {
                                        onPick(letter)
                                    }
                                    draggingLetter = null
                                    dragOffset = Offset.Zero
                                },
                                onDragCancel = {
                                    draggingLetter = null
                                    dragOffset = Offset.Zero
                                    isHoveringBlank = false
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
                                        .padding(bottom = metrics.spacing)
                                        .clickable { audio.playWord(round.word, rate = 0.9f) },
                                    contentScale = ContentScale.Fit
                                )
                            } else if (round.icon != null) {
                                FarmIcon(
                                    round.icon,
                                    modifier = Modifier
                                        .size(metrics.largePictureSize)
                                        .padding(bottom = metrics.spacing)
                                        .clickable { audio.playWord(round.word, rate = 0.9f) }
                                )
                            }
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(metrics.gridSpacing, Alignment.CenterHorizontally),
                                verticalArrangement = Arrangement.spacedBy(metrics.gridSpacing)
                            ) {
                                round.word.forEachIndexed { i, c ->
                                    if (i == blankIndex) {
                                        LetterChip(
                                            letter = filledLetter ?: ' ',
                                            size = metrics.chipSize,
                                            state = when {
                                                filledLetter != null -> ChoiceState.CORRECT
                                                isHoveringBlank -> ChoiceState.SELECTED
                                                else -> ChoiceState.IDLE
                                            },
                                            modifier = Modifier
                                                .padding(3.dp)
                                                .onGloballyPositioned { coords ->
                                                    val container = containerCoords ?: return@onGloballyPositioned
                                                    val center = Offset(coords.size.width / 2f, coords.size.height / 2f)
                                                    blankCenter = container.localPositionOf(coords, center)
                                                    blankRadius = coords.size.width / 2f * 1.4f
                                                }
                                        )
                                    } else {
                                        LetterChip(letter = c, size = metrics.chipSize, modifier = Modifier.padding(3.dp))
                                    }
                                }
                            }
                        },
                        choices = {
                            Text(
                                "Drag the missing beginning or ending letter",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = metrics.spacing)
                            )
                            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                                val rowSpacing = metrics.gridSpacing
                                val maxChoiceSize = ((maxWidth - rowSpacing * (CHOICE_COUNT - 1)) / CHOICE_COUNT)
                                    .coerceAtMost(metrics.choiceChipSize)
                                    .coerceAtLeast(44.dp)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(rowSpacing, Alignment.CenterHorizontally),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    choices.forEach { c ->
                                        val isDragging = draggingLetter == c
                                        LetterChip(
                                            letter = c,
                                            size = maxChoiceSize,
                                            state = when {
                                                isDragging -> ChoiceState.SELECTED
                                                wrongLetter == c -> ChoiceState.WRONG
                                                else -> ChoiceState.IDLE
                                            },
                                            modifier = Modifier
                                                .onGloballyPositioned { coords ->
                                                    if (c == draggingLetter) return@onGloballyPositioned
                                                    val container = containerCoords ?: return@onGloballyPositioned
                                                    val center = Offset(coords.size.width / 2f, coords.size.height / 2f)
                                                    chipPositions[c] = container.localPositionOf(coords, center)
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
