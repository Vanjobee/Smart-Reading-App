package com.sgbread.app.screens.module4

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.sgbread.app.audio.AudioManager
import com.sgbread.app.audio.Sfx
import com.sgbread.app.data.LettersBank
import com.sgbread.app.data.Praise
import com.sgbread.app.ui.components.ActivityCompleteOverlay
import com.sgbread.app.ui.components.ActivityScaffold
import com.sgbread.app.ui.components.AnswerFeedback
import com.sgbread.app.ui.components.ChoiceState
import com.sgbread.app.ui.components.PictureChoiceCard
import com.sgbread.app.ui.components.ResponsiveColumn
import com.sgbread.app.ui.components.activityLayoutMetrics
import com.sgbread.app.ui.theme.CreamWhite
import com.sgbread.app.ui.theme.RiceGreenDark
import com.sgbread.app.ui.theme.SgbReadTheme
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.random.Random

private val huntWords = LettersBank.patternWords
    .filter { it.image != null && it.audio != null }
    .distinctBy { it.word }
private const val DIGRAPH_HUNT_CHOICE_COUNT = 6

private fun scatterPositions(
    count: Int,
    boundsWidthPx: Float,
    boundsHeightPx: Float,
    cardPx: Float,
    seed: Int
): List<Offset> {
    val random = Random(seed)
    if (count <= 0 || boundsWidthPx <= 0f || boundsHeightPx <= 0f) return emptyList()

    val edgePadding = cardPx * 0.16f
    val minDistance = cardPx * 1.12f
    val maxX = (boundsWidthPx - cardPx - edgePadding).coerceAtLeast(edgePadding)
    val maxY = (boundsHeightPx - cardPx - edgePadding).coerceAtLeast(edgePadding)
    val positions = mutableListOf<Offset>()

    repeat(count) {
        var bestCandidate = Offset(edgePadding, edgePadding)
        var bestDistance = -1f
        var attempts = 0
        while (attempts < 260) {
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

/**
 * One target word at a time: the child sees/hears the word, then picks the
 * matching picture from scattered image choices.
 */
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun DigraphHuntScreen(audio: AudioManager?, onComplete: () -> Unit, onBack: () -> Unit) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val responsiveTextScale = (screenWidthDp / 360f).coerceIn(1f, 1.25f)

    // Only words with real image assets are used here because this activity's choices
    // are picture cards, not text/digraph cards.
    val huntRounds = remember { huntWords.filter { it.image != null }.shuffled() }
    var roundIndex by remember { mutableStateOf(0) }
    var feedback by remember { mutableStateOf<AnswerFeedback>(AnswerFeedback.None) }
    var wrongWord by remember { mutableStateOf<String?>(null) }
    var pendingPraise by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }
    var inputLocked by remember { mutableStateOf(false) }
    val draggedOffsets = remember(roundIndex) { mutableStateMapOf<String, Offset>() }
    var draggingWord by remember(roundIndex) { mutableStateOf<String?>(null) }

    val target = huntRounds[roundIndex]
    val choices = remember(roundIndex) {
        val distractors = huntWords
            .filter { it.image != null && it.word != target.word }
            .shuffled()
            .take(DIGRAPH_HUNT_CHOICE_COUNT - 1)
        (distractors + target).shuffled()
    }

    fun speakPrompt() = audio.playPatternWord(target, rate = 0.9f)
    LaunchedEffect(roundIndex) {
        wrongWord = null
        inputLocked = false
        speakPrompt()
    }

    fun onPick(word: String) {
        if (inputLocked || feedback !is AnswerFeedback.None) return
        inputLocked = true
        draggingWord = null
        audio?.stopPlayback()
        if (word == target.word) {
            audio.playPatternWord(target, rate = 0.9f) {
                pendingPraise = true
            }
        } else {
            wrongWord = word
            val wrongChoice = choices.firstOrNull { it.word == word }
            audio.playPatternWord(wrongChoice ?: target, rate = 0.9f) {
                audio?.playSfx(Sfx.INCORRECT)
                feedback = AnswerFeedback.Incorrect(Praise.randomEncouragement())
            }
        }
    }

    LaunchedEffect(pendingPraise) {
        if (pendingPraise) {
            delay(250)
            audio?.playSfx(Sfx.CORRECT)
            feedback = AnswerFeedback.Correct(Praise.randomCorrect())
            pendingPraise = false
        }
    }

    LaunchedEffect(feedback) {
        val current = feedback
        if (current is AnswerFeedback.Correct) {
            delay(850)
            while (audio?.isPlaying?.value == true) delay(100)
            feedback = AnswerFeedback.None
            if (roundIndex == huntRounds.lastIndex) {
                audio?.playSfx(Sfx.HARVEST)
                finished = true
            } else {
                roundIndex += 1
            }
        } else if (current is AnswerFeedback.Incorrect) {
            delay(750)
            feedback = AnswerFeedback.None
            wrongWord = null
            inputLocked = false
        }
    }

    ActivityScaffold(
        title = "Digraph Hunt",
        onBack = onBack,
        onReplayInstructions = { speakPrompt() },
        feedback = feedback,
        audio = audio,
        blockInputDuringAudio = false,
        titleTextScale = responsiveTextScale,
        confirmOnBack = roundIndex > 0 || (inputLocked && wrongWord == null)
    ) { padding ->
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val metrics = activityLayoutMetrics(maxWidth, maxHeight)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.42f))
            )
            ResponsiveColumn(metrics = metrics, modifier = Modifier.padding(padding)) {
                Text(
                    "Round ${roundIndex + 1} of ${huntRounds.size}",
                    style = MaterialTheme.typography.bodyMedium.let { baseStyle ->
                        baseStyle.copy(
                            color = CreamWhite,
                            fontSize = baseStyle.fontSize * responsiveTextScale
                        )
                    }
                )
                Text(
                    "Find the picture you hear",
                    style = MaterialTheme.typography.titleLarge.let { baseStyle ->
                        baseStyle.copy(
                            color = CreamWhite,
                            fontSize = baseStyle.fontSize * responsiveTextScale
                        )
                    },
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                @Composable
                fun QuestionPane() {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(metrics.spacing)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.66f)
                                .background(CreamWhite, RoundedCornerShape(22.dp))
                                .border(4.dp, RiceGreenDark, RoundedCornerShape(22.dp))
                                .clickable {
                                    audio?.stopPlayback()
                                    speakPrompt()
                                }
                                .padding(horizontal = 18.dp, vertical = if (metrics.compactHeight) 12.dp else 18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                target.word,
                                fontSize = (if (metrics.compactHeight) 30.sp else 38.sp) * responsiveTextScale,
                                fontWeight = FontWeight.ExtraBold,
                                color = RiceGreenDark,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                @Composable
                fun ChoicesPane() {
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(CreamWhite.copy(alpha = 0.28f), RoundedCornerShape(24.dp))
                            .padding(if (metrics.compactHeight) 6.dp else 10.dp)
                    ) {
                        val density = LocalDensity.current
                        val imageSize = if (metrics.compactWidth || metrics.compactHeight) {
                            metrics.pictureSize * 0.72f
                        } else {
                            metrics.choiceImageSize * 0.82f
                        }
                        val cardPx = with(density) { (imageSize + 32.dp).toPx() }
                        val widthPx = with(density) { maxWidth.toPx() }
                        val heightPx = with(density) { maxHeight.toPx() }
                        val positions = remember(roundIndex, widthPx, heightPx, cardPx) {
                            scatterPositions(choices.size, widthPx, heightPx, cardPx, seed = roundIndex + 113)
                        }
                        choices.forEachIndexed { index, w ->
                            val pos = positions.getOrElse(index) { Offset.Zero }
                            val dragOffset = draggedOffsets[w.word] ?: Offset.Zero
                            val isDragging = draggingWord == w.word
                            PictureChoiceCard(
                                icon = w.icon,
                                image = w.image,
                                label = null,
                                imageSize = imageSize,
                                state = when {
                                    w.word == target.word && feedback is AnswerFeedback.Correct -> ChoiceState.CORRECT
                                    wrongWord == w.word -> ChoiceState.WRONG
                                    else -> ChoiceState.IDLE
                                },
                                enabled = !inputLocked && feedback is AnswerFeedback.None,
                                onClick = { onPick(w.word) },
                                modifier = Modifier
                                    .offset {
                                        IntOffset(
                                            (pos.x + dragOffset.x).roundToInt(),
                                            (pos.y + dragOffset.y).roundToInt()
                                        )
                                    }
                                    .zIndex(if (isDragging) 1f else 0f)
                                    .graphicsLayer {
                                        val pickedUpScale = if (isDragging) 1.08f else 1f
                                        scaleX = pickedUpScale
                                        scaleY = pickedUpScale
                                    }
                                    .pointerInput(roundIndex, w.word, widthPx, heightPx, cardPx, inputLocked) {
                                        detectDragGestures(
                                            onDragStart = {
                                                if (!inputLocked) draggingWord = w.word
                                            },
                                            onDrag = { change, dragAmount ->
                                                if (inputLocked || draggingWord != w.word) {
                                                    return@detectDragGestures
                                                }
                                                change.consume()
                                                val currentOffset = draggedOffsets[w.word] ?: Offset.Zero
                                                val currentPosition = pos + currentOffset
                                                val nextPosition = Offset(
                                                    x = (currentPosition.x + dragAmount.x)
                                                        .coerceIn(0f, (widthPx - cardPx).coerceAtLeast(0f)),
                                                    y = (currentPosition.y + dragAmount.y)
                                                        .coerceIn(0f, (heightPx - cardPx).coerceAtLeast(0f))
                                                )
                                                draggedOffsets[w.word] = nextPosition - pos
                                            },
                                            onDragEnd = { draggingWord = null },
                                            onDragCancel = { draggingWord = null }
                                        )
                                    }
                            )
                        }
                    }
                }

                // Question/prompt on top, choices below -- portrait-only, top-to-bottom flow.
                QuestionPane()
                ChoicesPane()
            }
        }

        if (finished) {
            ActivityCompleteOverlay(onContinue = onComplete)
        }
    }
}

@Preview(device = "spec:width=360dp,height=800dp,orientation=portrait", showBackground = true)
@Composable
private fun DigraphHuntScreenPreview() {
    SgbReadTheme {
        DigraphHuntScreen(
            audio = null,
            onComplete = {},
            onBack = {}
        )
    }
}
