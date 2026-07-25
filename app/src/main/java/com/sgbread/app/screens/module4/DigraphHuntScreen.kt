package com.sgbread.app.screens.module4

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlin.random.Random

private val huntWords = LettersBank.patternWords.distinctBy { it.word }
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
 * One target word at a time: the child sees/hears the target digraph, then picks the
 * matching word from two large picture choices (1 correct + 1 distractor).
 */
@Composable
fun DigraphHuntScreen(audio: AudioManager, onComplete: () -> Unit, onBack: () -> Unit) {
    // Every pattern in the bank is fair game now that a round only needs one matching
    // word (not "find all"); freshly shuffled each time the screen is entered.
    val huntPatterns = remember { huntWords.map { it.pattern }.distinct().shuffled().take(10) }
    var roundIndex by remember { mutableStateOf(0) }
    var feedback by remember { mutableStateOf<AnswerFeedback>(AnswerFeedback.None) }
    var wrongWord by remember { mutableStateOf<String?>(null) }
    var pendingPraise by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }

    val targetPattern = huntPatterns[roundIndex]
    val target = remember(roundIndex) { huntWords.filter { it.pattern == targetPattern }.random() }
    val choices = remember(roundIndex) {
        val distractors = huntWords
            .filter { it.pattern != targetPattern }
            .shuffled()
            .take(DIGRAPH_HUNT_CHOICE_COUNT - 1)
        (distractors + target).shuffled()
    }

    fun speakPrompt() = audio.playPatternSound(targetPattern)
    LaunchedEffect(roundIndex) {
        wrongWord = null
        speakPrompt()
    }

    fun onPick(word: String) {
        audio.stopPlayback()
        if (word == target.word) {
            audio.playWord(target.word, rate = 0.9f) {
                pendingPraise = true
            }
        } else {
            audio.playSfx(Sfx.INCORRECT)
            wrongWord = word
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
            if (roundIndex == huntPatterns.lastIndex) {
                audio.playSfx(Sfx.HARVEST)
                finished = true
            } else {
                roundIndex += 1
            }
        } else if (current is AnswerFeedback.Incorrect) {
            delay(750)
            feedback = AnswerFeedback.None
            wrongWord = null
        }
    }

    ActivityScaffold(
        title = "Digraph Hunt",
        onBack = onBack,
        onReplayInstructions = { speakPrompt() },
        feedback = feedback,
        audio = audio,
        blockInputDuringAudio = false
    ) { padding ->
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val metrics = activityLayoutMetrics(maxWidth, maxHeight)
            ResponsiveColumn(metrics = metrics, modifier = Modifier.padding(padding)) {
                Text("Round ${roundIndex + 1} of ${huntPatterns.size}", style = MaterialTheme.typography.bodyMedium)

                @Composable
                fun QuestionPane() {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(metrics.spacing)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(if (metrics.compactHeight) 82.dp else 96.dp)
                                .background(CreamWhite, RoundedCornerShape(22.dp))
                                .border(4.dp, RiceGreenDark, RoundedCornerShape(22.dp))
                                .clickable {
                                    audio.stopPlayback()
                                    audio.playPatternSound(targetPattern)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                targetPattern,
                                fontSize = if (metrics.compactHeight) 34.sp else 40.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = RiceGreenDark
                            )
                        }
                        Text(
                            "Find the word with \"$targetPattern\"",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
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
                        val cardPx = with(density) { (imageSize + 28.dp).toPx() }
                        val widthPx = with(density) { maxWidth.toPx() }
                        val heightPx = with(density) { maxHeight.toPx() }
                        val positions = remember(roundIndex, widthPx, heightPx, cardPx) {
                            scatterPositions(choices.size, widthPx, heightPx, cardPx, seed = roundIndex + 113)
                        }
                        choices.forEachIndexed { index, w ->
                            val pos = positions.getOrElse(index) { Offset.Zero }
                            val offsetX = with(density) { pos.x.toDp() }
                            val offsetY = with(density) { pos.y.toDp() }
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
                                enabled = feedback is AnswerFeedback.None,
                                onClick = { onPick(w.word) },
                                modifier = Modifier.offset(x = offsetX, y = offsetY)
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
            audio = AudioManager.getInstance(LocalContext.current),
            onComplete = {},
            onBack = {}
        )
    }
}
