package com.sgbread.app.screens.module2

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.sgbread.app.audio.ActivityInstruction
import com.sgbread.app.audio.AudioManager
import com.sgbread.app.audio.Sfx
import com.sgbread.app.audio.playActivityInstruction
import com.sgbread.app.data.LettersBank
import com.sgbread.app.data.Praise
import com.sgbread.app.ui.components.ActivityCompleteOverlay
import com.sgbread.app.ui.components.ActivityScaffold
import com.sgbread.app.ui.components.AnswerFeedback
import com.sgbread.app.ui.components.ChoiceState
import com.sgbread.app.ui.components.LetterChip
import com.sgbread.app.ui.components.TwoPaneActivityBody
import com.sgbread.app.ui.components.activityLayoutMetrics
import com.sgbread.app.ui.theme.CreamWhite
import com.sgbread.app.ui.theme.SgbReadTheme
import kotlinx.coroutines.delay

// Module 2 uses larger touch targets/text than the shared activity defaults.
private const val M2_SCALE = 1.25f
private const val CHOICE_COUNT = 6
private val TAP_LETTER_EXCLUDED_WORDS = setOf("goat", "rice")

@OptIn(ExperimentalLayoutApi::class)
@SuppressLint("UnusedBoxWithConstraintsScope", "AutoboxingStateCreation")
@Composable
fun TapLetterScreen(audio: AudioManager?, onComplete: () -> Unit, onBack: () -> Unit) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val responsiveTextScale = (screenWidthDp / 360f).coerceIn(1f, 1.25f)

    // Freshly shuffled each time the screen is entered, not just once per app launch.
    val tapItems = remember { LettersBank.phonicsItems.filter { it.word !in TAP_LETTER_EXCLUDED_WORDS } }
    val rounds = remember { tapItems.shuffled().take(10) }
    var roundIndex by remember { mutableIntStateOf(0) }
    var feedback by remember { mutableStateOf<AnswerFeedback>(AnswerFeedback.None) }
    var wrongLetter by remember { mutableStateOf<Char?>(null) }
    var correctLetter by remember { mutableStateOf<Char?>(null) }
    var roundLocked by remember { mutableStateOf(false) }
    var pendingPraise by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }

    val round = rounds[roundIndex]
    val choices = remember(roundIndex) {
        (listOf(round.letter) + LettersBank.phonicsItems
            .map { it.letter }
            .filter { it != round.letter }
            .filter { letter -> tapItems.any { it.letter == letter } }
            .distinct()
            .shuffled()
            .take(CHOICE_COUNT - 1))
            .distinct()
            .shuffled()
    }

    fun speakPrompt() = audio?.playWord(round.word, rate = 0.85f)

    var hasIntroduced by remember { mutableStateOf(false) }
    LaunchedEffect(roundIndex) {
        wrongLetter = null
        correctLetter = null
        roundLocked = false
        if (!hasIntroduced) {
            hasIntroduced = true
            audio.playActivityInstruction(ActivityInstruction.PHONICS_MATCH) {
                speakPrompt()
            }
            return@LaunchedEffect
        }
        speakPrompt()
    }

    fun onPick(letter: Char) {
        if (roundLocked) return
        audio?.stopPlayback()
        if (letter == round.letter) {
            correctLetter = letter
            roundLocked = true
            audio?.speakLetterThenWord(round.letter, round.word, rate = 0.9f) {
                pendingPraise = true
            }
        } else {
            audio?.playSfx(Sfx.INCORRECT)
            audio?.playLetterSound(letter)
            wrongLetter = letter
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
            wrongLetter = null
        }
    }

    ActivityScaffold(
        title = "Phonics Match",
        onBack = onBack,
        onReplayInstructions = {
            audio.playActivityInstruction(ActivityInstruction.PHONICS_MATCH) {
                speakPrompt()
            }
        },
        feedback = feedback,
        audio = audio,
        blockInputDuringAudio = false,
        titleTextScale = responsiveTextScale,
        confirmOnBack = roundIndex > 0 || correctLetter != null
    ) { padding ->
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val metrics = activityLayoutMetrics(maxWidth, maxHeight)
            val responsiveImageScale = if (metrics.compactHeight) {
                1f
            } else {
                responsiveTextScale.coerceAtMost(1.15f)
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.42f))
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = metrics.horizontalPadding, vertical = metrics.verticalPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Round ${roundIndex + 1} of ${rounds.size}",
                    style = MaterialTheme.typography.titleMedium.let { baseStyle ->
                        baseStyle.copy(
                            color = CreamWhite,
                            fontSize = baseStyle.fontSize * responsiveTextScale
                        )
                    }
                )
                Text(
                    "Click the picture and listen, then find the first letter.",
                    style = MaterialTheme.typography.titleMedium.let { baseStyle ->
                        baseStyle.copy(
                            color = CreamWhite,
                            fontSize = baseStyle.fontSize * responsiveTextScale
                        )
                    },
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = metrics.spacing)
                )
                TwoPaneActivityBody(
                    metrics = metrics,
                    modifier = Modifier.weight(1f),
                    question = {
                        Image(
                            painter = painterResource(round.image),
                            contentDescription = round.word,
                            modifier = Modifier
                                .size(metrics.largePictureSize * M2_SCALE * responsiveImageScale)
                                .clickable {
                                    audio?.stopPlayback()
                                    audio?.playWord(round.word, rate = 0.9f)
                                },
                            contentScale = ContentScale.Fit
                        )
                    },
                    choices = {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(metrics.gridSpacing, Alignment.CenterHorizontally),
                            verticalArrangement = Arrangement.spacedBy(metrics.gridSpacing),
                            maxItemsInEachRow = 3
                        ) {
                            choices.forEach { c ->
                                LetterChip(
                                    letter = c,
                                    size = if (metrics.compactWidth || metrics.compactHeight) metrics.chipSize else metrics.choiceChipSize,
                                    state = when (c) {
                                        correctLetter -> ChoiceState.CORRECT
                                        wrongLetter -> ChoiceState.WRONG
                                        else -> ChoiceState.IDLE
                                    },
                                    enabled = !roundLocked,
                                    onClick = { onPick(c) }
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
private fun TapLetterScreenPreview() {
    SgbReadTheme {
        TapLetterScreen(audio = null, onComplete = {}, onBack = {})
    }
}
