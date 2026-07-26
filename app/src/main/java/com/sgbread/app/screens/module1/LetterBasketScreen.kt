package com.sgbread.app.screens.module1

import android.annotation.SuppressLint
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
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
import kotlin.random.Random

/** A round's target letter bundled with its choice set (target + 2 distractors), computed
 * together so the answer can never end up desynced from the picture it belongs to. */
private data class BasketRound(val target: PhonicsItem, val choices: List<BasketChoice>)
private data class BasketChoice(val id: Int, val letter: Char, val isTarget: Boolean, val found: Boolean = false)

private const val WORD_POPUP_HOLD_AFTER_AUDIO_MS = 450L
private const val BASKET_CHOICE_COUNT = 18
private const val BASKET_TARGET_COUNT = 6
private val LETTER_BASKET_EXCLUDED_WORDS = setOf("goat", "rice")
private val LETTER_BASKET_EXCLUDED_LETTERS = setOf('G', 'R')

private fun buildBasketChoices(target: Char): List<BasketChoice> {
    val distractors = LettersBank.phonicsItems
        .filter { it.word !in LETTER_BASKET_EXCLUDED_WORDS }
        .map { it.letter }
        .filter { it != target && it.uppercaseChar() !in LETTER_BASKET_EXCLUDED_LETTERS }
    val targetChoices = List(BASKET_TARGET_COUNT) { index ->
        BasketChoice(
            id = index,
            letter = if (index % 2 == 0) target.uppercaseChar() else target.lowercaseChar(),
            isTarget = true
        )
    }
    val distractorChoices = List(BASKET_CHOICE_COUNT - BASKET_TARGET_COUNT) { index ->
        val letter = distractors.random()
        BasketChoice(
            id = BASKET_TARGET_COUNT + index,
            letter = if (Random.nextBoolean()) letter.uppercaseChar() else letter.lowercaseChar(),
            isTarget = false
        )
    }
    return (targetChoices + distractorChoices).shuffled()
}

private fun scatterPositions(
    count: Int,
    boundsWidthPx: Float,
    boundsHeightPx: Float,
    chipPx: Float,
    seed: Int
): List<Offset> {
    val random = Random(seed)
    if (count <= 0 || boundsWidthPx <= 0f || boundsHeightPx <= 0f) return emptyList()

    val edgePadding = chipPx * 0.22f
    val minDistance = chipPx * 1.08f
    val maxX = (boundsWidthPx - chipPx - edgePadding).coerceAtLeast(edgePadding)
    val maxY = (boundsHeightPx - chipPx - edgePadding).coerceAtLeast(edgePadding)
    val positions = mutableListOf<Offset>()

    repeat(count) {
        var bestCandidate = Offset(edgePadding, edgePadding)
        var bestDistance = -1f
        var attempts = 0

        while (attempts < 120) {
            val candidate = Offset(
                x = edgePadding + random.nextFloat() * (maxX - edgePadding).coerceAtLeast(0f),
                y = edgePadding + random.nextFloat() * (maxY - edgePadding).coerceAtLeast(0f)
            )
            val nearestDistance = positions.minOfOrNull { (it - candidate).getDistance() } ?: Float.MAX_VALUE
            if (nearestDistance >= minDistance) {
                bestCandidate = candidate
                break
            }
            if (nearestDistance > bestDistance) {
                bestCandidate = candidate
                bestDistance = nearestDistance
            }
            attempts++
        }

        positions.add(bestCandidate)
    }

    return positions
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LetterBasketScreen(audio: AudioManager?, onComplete: () -> Unit, onBack: () -> Unit) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val responsiveTextScale = (screenWidthDp / 360f).coerceIn(1f, 1.25f)

    // Freshly shuffled each time the screen is entered, so replays don't always start on A-C.
    val basketRounds = remember {
        LettersBank.phonicsItems
            .filter { it.word !in LETTER_BASKET_EXCLUDED_WORDS }
            .shuffled()
            .take(10)
            .map { target ->
                BasketRound(target, buildBasketChoices(target.letter))
            }
    }
    var roundIndex by remember { mutableStateOf(0) }
    val round = basketRounds[roundIndex]
    val target = round.target
    var choices by remember(roundIndex) { mutableStateOf(buildBasketChoices(target.letter)) }
    val foundCount = choices.count { it.isTarget && it.found }
    val targetPair = "${target.letter.uppercaseChar()}${target.letter.lowercaseChar()}"

    var feedback by remember { mutableStateOf<AnswerFeedback>(AnswerFeedback.None) }
    var lockInput by remember { mutableStateOf(false) }
    var lastWrongChoiceId by remember { mutableStateOf<Int?>(null) }
    var finished by remember { mutableStateOf(false) }
    // Reward popup shown on a correct match -- reveals the sample word this letter
    // stands for, which is otherwise never shown up front (no more reference picture).
    var wordPopupVisible by remember { mutableStateOf(false) }
    var wordSoundFinished by remember { mutableStateOf(false) }

    var draggingChoiceId by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var isHoveringBasket by remember { mutableStateOf(false) }
    val chipPositions = remember { mutableMapOf<Int, Offset>() }
    var containerCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var basketCenter by remember { mutableStateOf<Offset?>(null) }
    var basketRadius by remember { mutableStateOf(0f) }

    LaunchedEffect(roundIndex) {
        chipPositions.clear()
        draggingChoiceId = null
        dragOffset = Offset.Zero
        isHoveringBasket = false
    }

    fun instructions() = audio?.playRecordedPrompt(
        "Tap the sample letter to hear it, then drag its match into the basket.",
        rate = 0.9f
    )
    LaunchedEffect(Unit) { instructions() }

    fun evaluate(choiceId: Int) {
        val choice = choices.firstOrNull { it.id == choiceId } ?: return
        if (choice.isTarget) {
            audio?.playSfx(Sfx.CORRECT)
            val updatedChoices = choices.map {
                if (it.id == choiceId) it.copy(found = true) else it
            }
            choices = updatedChoices
            draggingChoiceId = null
            dragOffset = Offset.Zero
            if (foundCount + 1 == BASKET_TARGET_COUNT) {
                lockInput = true
                wordPopupVisible = true
                wordSoundFinished = false
                audio?.speakLetterNameThenWord(target.letter, target.word, rate = 0.9f, onComplete = {
                    wordSoundFinished = true
                })
            }
        } else {
            audio?.playSfx(Sfx.INCORRECT)
            lastWrongChoiceId = choiceId
            feedback = AnswerFeedback.Incorrect("Try another letter!")
        }
    }

    LaunchedEffect(wordSoundFinished) {
        if (wordSoundFinished) {
            delay(WORD_POPUP_HOLD_AFTER_AUDIO_MS)
            wordPopupVisible = false
            wordSoundFinished = false
            lockInput = false
            draggingChoiceId = null
            dragOffset = Offset.Zero
            if (roundIndex == basketRounds.size - 1) {
                audio?.playSfx(Sfx.HARVEST)
                finished = true
            } else {
                roundIndex += 1
            }
        }
    }

    LaunchedEffect(feedback) {
        if (feedback is AnswerFeedback.Incorrect) {
            delay(750)
            feedback = AnswerFeedback.None
            lockInput = false
            lastWrongChoiceId = null
            draggingChoiceId = null
            dragOffset = Offset.Zero
        }
    }

    ActivityScaffold(
        title = "Letter Hunt",
        onBack = onBack,
        onReplayInstructions = { instructions() },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 8.dp, vertical = if (metrics.compactHeight) 2.dp else 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Drag all matching letters into the basket",
                    style = (
                        if (metrics.compactHeight) {
                            MaterialTheme.typography.titleSmall
                        } else {
                            MaterialTheme.typography.titleMedium
                        }
                    ).let { baseStyle ->
                        baseStyle.copy(
                            color = CreamWhite,
                            fontSize = baseStyle.fontSize * responsiveTextScale
                        )
                    },
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = if (metrics.compactHeight) 1.dp else 4.dp)
                )
                Text(
                    "Round ${roundIndex + 1} of ${basketRounds.size} • Found $foundCount / $BASKET_TARGET_COUNT",
                    style = MaterialTheme.typography.labelMedium.let { baseStyle ->
                        baseStyle.copy(
                            color = CreamWhite,
                            fontSize = baseStyle.fontSize * responsiveTextScale
                        )
                    },
                    modifier = Modifier.padding(bottom = if (metrics.compactHeight) 4.dp else 8.dp)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .onGloballyPositioned { containerCoords = it }
                        .pointerInput(roundIndex, metrics.chipSize, choices) {
                            val chipSizePx = (metrics.chipSize * 0.86f).toPx()
                            val startHitRadius = maxOf(36.dp.toPx(), chipSizePx * 0.9f)
                            detectDragGestures(
                            onDragStart = { offset ->
                                if (lockInput) return@detectDragGestures
                                // Only consider chips belonging to the current round: stale
                                // positions from earlier rounds can otherwise "win" the nearest
                                // check and silently grab a letter that isn't even on screen.
                                val nearest = chipPositions.entries
                                    .filter { (id, _) -> choices.any { it.id == id && !it.found } }
                                    .minByOrNull { (_, pos) -> (pos - offset).getDistance() }
                                if (nearest != null && (nearest.value - offset).getDistance() < startHitRadius) {
                                    val choice = choices.first { it.id == nearest.key }
                                audio?.stopPlayback()
                                    draggingChoiceId = choice.id
                                    dragOffset = Offset.Zero
                                    audio?.playLetterName(choice.letter.uppercaseChar())
                                }
                            },
                            onDrag = { _, dragAmount ->
                                if (draggingChoiceId != null) {
                                    dragOffset += dragAmount
                                    val start = chipPositions[draggingChoiceId]
                                    val center = basketCenter
                                    if (start != null && center != null) {
                                        isHoveringBasket = (start + dragOffset - center).getDistance() < basketRadius
                                    }
                                }
                            },
                            onDragEnd = {
                                val choiceId = draggingChoiceId
                                val start = choiceId?.let { chipPositions[it] }
                                val center = basketCenter
                                isHoveringBasket = false
                                if (choiceId != null && start != null && center != null &&
                                    (start + dragOffset - center).getDistance() < basketRadius
                                ) {
                                    evaluate(choiceId)
                                } else {
                                    draggingChoiceId = null
                                    dragOffset = Offset.Zero
                                }
                            },
                            onDragCancel = {
                                draggingChoiceId = null
                                dragOffset = Offset.Zero
                                isHoveringBasket = false
                            }
                            )
                        }
                ) {
                    // Enlarged for visibility and touch target size -- this single element is
                    // now both the "hear the sound" button and the drag-drop target, so it
                    // needs to read clearly on its own without a separate sample-letter tile.
                    val basketSize = if (metrics.compactWidth || metrics.compactHeight) 132.dp else 190.dp
                    val basketFontSize = if (metrics.compactWidth || metrics.compactHeight) 48.sp else 72.sp

                    @Composable
                    fun SampleLetterAndBasket() {
                        BasketWithLetter(
                            letter = targetPair,
                            state = when {
                                wordPopupVisible -> ChoiceState.CORRECT
                                isHoveringBasket -> ChoiceState.SELECTED
                                else -> ChoiceState.IDLE
                            },
                            size = basketSize,
                            fontSize = basketFontSize,
                            onClick = {
                                if (!lockInput) {
                                    audio?.stopPlayback()
                                    audio?.playLetterName(target.letter)
                                }
                            },
                            modifier = Modifier.onGloballyPositioned { coords ->
                                val container = containerCoords ?: return@onGloballyPositioned
                                val center = Offset(coords.size.width / 2f, coords.size.height / 2f)
                                basketCenter = container.localPositionOf(coords, center)
                                basketRadius = coords.size.width / 2f * 1.3f
                            }
                        )
                    }

                    @Composable
                    fun Choices(modifier: Modifier = Modifier) {
                        BoxWithConstraints(
                            modifier = modifier
                                .fillMaxWidth()
                                .background(CreamWhite.copy(alpha = 0.28f), RoundedCornerShape(24.dp))
                                .padding(if (metrics.compactHeight) 6.dp else 10.dp)
                        ) {
                            val visibleChoices = choices.filterNot { it.found }
                            val density = LocalDensity.current
                            val chipSize = if (metrics.compactWidth || metrics.compactHeight) {
                                metrics.chipSize * 0.78f
                            } else {
                                metrics.chipSize * 0.86f
                            }
                            val chipPx = with(density) { chipSize.toPx() }
                            val widthPx = with(density) { maxWidth.toPx() }
                            val heightPx = with(density) { maxHeight.toPx() }
                            val positions = remember(roundIndex, visibleChoices.map { it.id }, widthPx, heightPx, chipPx) {
                                scatterPositions(visibleChoices.size, widthPx, heightPx, chipPx, seed = roundIndex + 71)
                            }
                            visibleChoices.forEachIndexed { index, choice ->
                                val isDragging = draggingChoiceId == choice.id
                                val pos = positions.getOrElse(index) { Offset.Zero }
                                val offsetX = with(density) { pos.x.toDp() }
                                val offsetY = with(density) { pos.y.toDp() }
                                LetterChip(
                                    letter = choice.letter,
                                    size = chipSize,
                                    state = when {
                                        isDragging -> ChoiceState.SELECTED
                                        lastWrongChoiceId == choice.id -> ChoiceState.WRONG
                                        else -> ChoiceState.IDLE
                                    },
                                    modifier = Modifier
                                        .offset(x = offsetX, y = offsetY)
                                        .onGloballyPositioned { coords ->
                                            if (choice.id == draggingChoiceId) return@onGloballyPositioned
                                            val container = containerCoords ?: return@onGloballyPositioned
                                            val center = Offset(coords.size.width / 2f, coords.size.height / 2f)
                                            chipPositions[choice.id] = container.localPositionOf(coords, center)
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
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(if (metrics.compactHeight) 4.dp else metrics.spacing)
                    ) {
                        SampleLetterAndBasket()
                        Choices(Modifier.weight(1f))
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
                            .padding(if (metrics.compactHeight) 22.dp else 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(target.image),
                            contentDescription = target.word,
                            modifier = Modifier.size(if (metrics.compactHeight) metrics.largePictureSize else metrics.largePictureSize * 1.15f),
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
    letter: String,
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
            letter,
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
        LetterBasketScreen(audio = null, onComplete = {}, onBack = {})
    }
}
