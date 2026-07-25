package com.sgbread.app.screens.module1

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sgbread.app.audio.AudioManager
import com.sgbread.app.audio.Sfx
import com.sgbread.app.data.LettersBank
import com.sgbread.app.data.PhonicsItem
import com.sgbread.app.ui.components.ActivityCompleteOverlay
import com.sgbread.app.ui.components.ActivityScaffold
import com.sgbread.app.ui.components.AnswerFeedback
import com.sgbread.app.ui.components.BasketDropTarget
import com.sgbread.app.ui.components.ChoiceState
import com.sgbread.app.ui.components.LetterChip
import com.sgbread.app.ui.components.activityLayoutMetrics
import com.sgbread.app.ui.theme.CreamWhite
import com.sgbread.app.ui.theme.RiceGreenDark
import com.sgbread.app.ui.theme.SgbReadTheme
import com.sgbread.app.ui.theme.SoilBrown
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/** A round's target letter bundled with its choice set (target + 2 distractors), computed
 * together so the answer can never end up desynced from the picture it belongs to. */
private data class BasketRound(val target: PhonicsItem, val choices: List<Char>)

private const val WORD_POPUP_MS = 1900L

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LetterBasketScreen(audio: AudioManager, onComplete: () -> Unit, onBack: () -> Unit) {
    // Freshly shuffled each time the screen is entered, so replays don't always start on A-C.
    val basketRounds = remember {
        LettersBank.phonicsItems.shuffled().take(10).map { target ->
            val distractors = LettersBank.phonicsItems
                .filter { it.letter != target.letter }
                .shuffled()
                .take(1)
                .map { it.letter }
            BasketRound(target, (listOf(target.letter) + distractors).shuffled())
        }
    }
    var roundIndex by remember { mutableStateOf(0) }
    val round = basketRounds[roundIndex]
    val target = round.target
    val choices = round.choices

    var feedback by remember { mutableStateOf<AnswerFeedback>(AnswerFeedback.None) }
    var lockInput by remember { mutableStateOf(false) }
    var lastWrongLetter by remember { mutableStateOf<Char?>(null) }
    var finished by remember { mutableStateOf(false) }
    // Reward popup shown on a correct match -- reveals the sample word this letter
    // stands for, which is otherwise never shown up front (no more reference picture).
    var wordPopupVisible by remember { mutableStateOf(false) }

    var draggingLetter by remember { mutableStateOf<Char?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var isHoveringBasket by remember { mutableStateOf(false) }
    val chipPositions = remember { mutableMapOf<Char, Offset>() }
    var containerCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var basketCenter by remember { mutableStateOf<Offset?>(null) }
    var basketRadius by remember { mutableStateOf(0f) }

    fun instructions() = audio.playRecordedPrompt(
        "Tap the sample letter to hear it, then drag its match into the basket.",
        rate = 0.9f
    )
    LaunchedEffect(Unit) { instructions() }

    fun evaluate(letter: Char) {
        if (letter == target.letter) {
            lockInput = true
            audio.playSfx(Sfx.CORRECT)
            // Sample Word audio plays as soon as the popup appears.
            audio.playWord(target.word, rate = 0.9f)
            wordPopupVisible = true
        } else {
            audio.playSfx(Sfx.INCORRECT)
            lastWrongLetter = letter
            feedback = AnswerFeedback.Incorrect("Try another letter!")
        }
    }

    LaunchedEffect(wordPopupVisible) {
        if (wordPopupVisible) {
            delay(WORD_POPUP_MS)
            wordPopupVisible = false
            lockInput = false
            draggingLetter = null
            dragOffset = Offset.Zero
            if (roundIndex == basketRounds.size - 1) {
                audio.playSfx(Sfx.HARVEST)
                finished = true
            } else {
                roundIndex += 1
            }
        }
    }

    LaunchedEffect(feedback) {
        if (feedback is AnswerFeedback.Incorrect) {
            delay(900)
            feedback = AnswerFeedback.None
            lockInput = false
            lastWrongLetter = null
            draggingLetter = null
            dragOffset = Offset.Zero
        }
    }

    ActivityScaffold(
        title = "Letter Basket",
        onBack = onBack,
        onReplayInstructions = { instructions() },
        feedback = feedback,
        audio = audio
    ) { padding ->
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val metrics = activityLayoutMetrics(maxWidth, maxHeight)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Tap the letter, then drag its match into the basket",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    "Round ${roundIndex + 1} of ${basketRounds.size}",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .onGloballyPositioned { containerCoords = it }
                        .pointerInput(roundIndex, metrics.chipSize) {
                            val startHitRadius = maxOf(48.dp.toPx(), metrics.chipSize.toPx() * 0.9f)
                            detectDragGestures(
                            onDragStart = { offset ->
                                if (lockInput) return@detectDragGestures
                                // Only consider chips belonging to the current round: stale
                                // positions from earlier rounds can otherwise "win" the nearest
                                // check and silently grab a letter that isn't even on screen.
                                val nearest = chipPositions.entries
                                    .filter { (letter, _) -> letter in choices }
                                    .minByOrNull { (_, pos) -> (pos - offset).getDistance() }
                                if (nearest != null && (nearest.value - offset).getDistance() < startHitRadius) {
                                    draggingLetter = nearest.key
                                    dragOffset = Offset.Zero
                                    audio.playLetterName(nearest.key)
                                }
                            },
                            onDrag = { _, dragAmount ->
                                if (draggingLetter != null) {
                                    dragOffset += dragAmount
                                    val start = chipPositions[draggingLetter]
                                    val center = basketCenter
                                    if (start != null && center != null) {
                                        isHoveringBasket = (start + dragOffset - center).getDistance() < basketRadius
                                    }
                                }
                            },
                            onDragEnd = {
                                val letter = draggingLetter
                                val start = letter?.let { chipPositions[it] }
                                val center = basketCenter
                                isHoveringBasket = false
                                if (letter != null && start != null && center != null &&
                                    (start + dragOffset - center).getDistance() < basketRadius
                                ) {
                                    evaluate(letter)
                                } else {
                                    draggingLetter = null
                                    dragOffset = Offset.Zero
                                }
                            },
                            onDragCancel = {
                                draggingLetter = null
                                dragOffset = Offset.Zero
                                isHoveringBasket = false
                            }
                            )
                        }
                ) {
                    // Enlarged for visibility and touch target size -- this single element is
                    // now both the "hear the sound" button and the drag-drop target, so it
                    // needs to read clearly on its own without a separate sample-letter tile.
                    val basketSize = if (metrics.compactWidth || metrics.compactHeight) 150.dp else 190.dp
                    val basketFontSize = if (metrics.compactWidth || metrics.compactHeight) 56.sp else 72.sp

                    @Composable
                    fun SampleLetterAndBasket() {
                        BasketWithLetter(
                            letter = target.letter,
                            state = when {
                                wordPopupVisible -> ChoiceState.CORRECT
                                isHoveringBasket -> ChoiceState.SELECTED
                                else -> ChoiceState.IDLE
                            },
                            size = basketSize,
                            fontSize = basketFontSize,
                            onClick = { audio.playLetterName(target.letter) },
                            modifier = Modifier.onGloballyPositioned { coords ->
                                val container = containerCoords ?: return@onGloballyPositioned
                                val center = Offset(coords.size.width / 2f, coords.size.height / 2f)
                                basketCenter = container.localPositionOf(coords, center)
                                basketRadius = coords.size.width / 2f * 1.3f
                            }
                        )
                    }

                    @Composable
                    fun Choices() {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
                        ) {
                            choices.forEach { letter ->
                                val isDragging = draggingLetter == letter
                                LetterChip(
                                    letter = letter,
                                    size = metrics.choiceChipSize,
                                    state = when {
                                        isDragging -> ChoiceState.SELECTED
                                        lastWrongLetter == letter -> ChoiceState.WRONG
                                        else -> ChoiceState.IDLE
                                    },
                                    modifier = Modifier
                                        .onGloballyPositioned { coords ->
                                            if (letter == draggingLetter) return@onGloballyPositioned
                                            val container = containerCoords ?: return@onGloballyPositioned
                                            val center = Offset(coords.size.width / 2f, coords.size.height / 2f)
                                            chipPositions[letter] = container.localPositionOf(coords, center)
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

                    // Portrait-only, top-to-bottom flow: the basket (with its sample letter)
                    // sits above the answer choices instead of splitting the width between them.
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(metrics.spacing)
                    ) {
                        SampleLetterAndBasket()
                        Choices()
                    }
                }
            }

            AnimatedVisibility(
                visible = wordPopupVisible,
                enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
                exit = scaleOut(),
                modifier = Modifier.matchParentSize()
            ) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color(0x99000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .background(CreamWhite, RoundedCornerShape(28.dp))
                            .padding(if (metrics.compactHeight) 16.dp else 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(target.image),
                            contentDescription = target.word,
                            modifier = Modifier.size(metrics.pictureSize),
                            contentScale = ContentScale.Fit
                        )
                        Text(
                            "\"${target.letter}\" is for \"${target.word}\"",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = SoilBrown,
                            modifier = Modifier.padding(top = metrics.gridSpacing)
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

/** The basket rendered as the background layer with the round's sample letter overlaid
 * directly on top of it, instead of two separate stacked tiles. Tap to hear the letter's
 * sound; drag a matching choice chip on top to drop it in. The text sits slightly below
 * center (bias 0.27) to land inside the basket's drawn bowl rather than its handle. */
@Composable
private fun BasketWithLetter(
    letter: Char,
    state: ChoiceState,
    size: Dp,
    fontSize: TextUnit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        BasketDropTarget(state = state, size = size)
        Text(
            letter.toString(),
            fontSize = fontSize,
            fontWeight = FontWeight.ExtraBold,
            color = RiceGreenDark,
            modifier = Modifier.align(BiasAlignment(0f, 0.27f))
        )
    }
}

@Preview(device = "spec:width=360dp,height=800dp,orientation=portrait", showBackground = true)
@Composable
private fun LetterBasketScreenPreview() {
    SgbReadTheme {
        LetterBasketScreen(audio = AudioManager.getInstance(androidx.compose.ui.platform.LocalContext.current), onComplete = {}, onBack = {})
    }
}
