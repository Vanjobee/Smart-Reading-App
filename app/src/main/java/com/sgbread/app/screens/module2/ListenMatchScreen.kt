package com.sgbread.app.screens.module2

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.sgbread.app.ui.components.TwoPaneActivityBody
import com.sgbread.app.ui.components.activityLayoutMetrics
import com.sgbread.app.ui.theme.CreamWhite
import com.sgbread.app.ui.theme.RiceGreenDark
import com.sgbread.app.ui.theme.SgbReadTheme
import kotlinx.coroutines.delay

// Module 2 uses larger touch targets/text than the shared activity defaults.
private const val M2_SCALE = 1.25f
private val LISTEN_MATCH_EXCLUDED_WORDS = setOf("goat", "rice")

@OptIn(ExperimentalLayoutApi::class)
@SuppressLint("UnusedBoxWithConstraintsScope", "AutoboxingStateCreation")
@Composable
fun ListenMatchScreen(audio: AudioManager?, onComplete: () -> Unit, onBack: () -> Unit) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val responsiveTextScale = (screenWidthDp / 360f).coerceIn(1f, 1.25f)

    // Freshly shuffled each time the screen is entered, not just once per app launch.
    val matchItems = remember { LettersBank.phonicsItems.filter { it.word !in LISTEN_MATCH_EXCLUDED_WORDS } }
    val rounds = remember { matchItems.shuffled().take(10) }
    var roundIndex by remember { androidx.compose.runtime.mutableIntStateOf(0) }
    var feedback by remember { mutableStateOf<AnswerFeedback>(AnswerFeedback.None) }
    var wrongPick by remember { mutableStateOf<Int?>(null) }
    var correctPick by remember { mutableStateOf<Int?>(null) }
    var roundLocked by remember { mutableStateOf(false) }
    var pendingPraise by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }

    val round = rounds[roundIndex]
    val choices = remember(roundIndex) {
        (listOf(round) + matchItems.filter { it != round }.shuffled().take(1)).shuffled()
    }

    fun speakPrompt() = audio?.speakThenLetterSound("Listen.", round.letter, rate = 0.85f)
    LaunchedEffect(roundIndex) {
        wrongPick = null
        correctPick = null
        roundLocked = false
        speakPrompt()
    }

    fun onPick(index: Int) {
        if (roundLocked) return
        val picked = choices[index]
        audio?.stopPlayback()
        if (picked.letter == round.letter) {
            correctPick = index
            roundLocked = true
            audio?.speakLetterThenWord(round.letter, round.word, rate = 0.9f) {
                pendingPraise = true
            }
        } else {
            audio?.playSfx(Sfx.INCORRECT)
            wrongPick = index
            feedback = AnswerFeedback.Incorrect(Praise.randomEncouragement())
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
            if (roundIndex == rounds.lastIndex) {
                audio?.playSfx(Sfx.HARVEST)
                finished = true
            } else {
                roundIndex += 1
            }
        } else if (current is AnswerFeedback.Incorrect) {
            delay(750)
            feedback = AnswerFeedback.None
            wrongPick = null
        }
    }

    ActivityScaffold(
        title = "Phonics Sound",
        onBack = onBack,
        onReplayInstructions = { speakPrompt() },
        feedback = feedback,
        audio = audio,
        blockInputDuringAudio = false,
        titleTextScale = responsiveTextScale,
        confirmOnBack = roundIndex > 0 || correctPick != null
    ) { padding ->
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val metrics = activityLayoutMetrics(maxWidth, maxHeight)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.42f))
            )
            ResponsiveColumn(
                metrics = metrics,
                modifier = Modifier.padding(padding),
                verticalArrangement = Arrangement.spacedBy(if (metrics.compactHeight) 4.dp else metrics.spacing),
                verticalPadding = if (metrics.compactHeight) 2.dp else metrics.verticalPadding
            ) {
                Text(
                    "Round ${roundIndex + 1} of ${rounds.size}",
                    style = (
                        if (metrics.compactHeight) {
                            MaterialTheme.typography.titleMedium
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
                    "Listen to the phonics sound, then choose its picture",
                    style = (
                        if (metrics.compactHeight) {
                            MaterialTheme.typography.headlineSmall
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
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = if (metrics.compactHeight) 2.dp else metrics.spacing)
                )
                TwoPaneActivityBody(
                    metrics = metrics,
                    modifier = Modifier.weight(1f),
                    question = {
                        Box(
                            modifier = Modifier
                                .size((if (metrics.compactHeight) 100.dp else 130.dp) * M2_SCALE)
                                .background(CreamWhite, RoundedCornerShape(28.dp))
                                .border(4.dp, RiceGreenDark, RoundedCornerShape(28.dp))
                                .clickable {
                                    audio?.stopPlayback()
                                    audio?.playLetterSound(round.letter)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                round.letter.uppercaseChar().toString(),
                                fontSize = ((if (metrics.compactHeight) 48f else 72f) * M2_SCALE).sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = RiceGreenDark
                            )
                        }

                    },
                    choices = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(metrics.gridSpacing),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            choices.forEachIndexed { i, item ->
                                PictureChoiceCard(
                                    image = item.image,
                                    label = item.word,
                                    imageSize = metrics.choiceImageSize,
                                    state = when {
                                        correctPick == i -> ChoiceState.CORRECT
                                        wrongPick == i -> ChoiceState.WRONG
                                        else -> ChoiceState.IDLE
                                    },
                                    enabled = !roundLocked,
                                    onClick = { onPick(i) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                )
            }
        }

        if (finished) {
            ActivityCompleteOverlay(onContinue = onComplete)
        }
    }
}

@Preview(device = "spec:width=360dp,height=800dp,orientation=portrait", showBackground = true)
@Composable
private fun ListenMatchScreenPreview() {
    SgbReadTheme {
        ListenMatchScreen(audio = null, onComplete = {}, onBack = {})
    }
}
