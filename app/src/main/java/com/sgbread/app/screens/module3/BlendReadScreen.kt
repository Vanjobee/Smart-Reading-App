package com.sgbread.app.screens.module3

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.sgbread.app.audio.ActivityInstruction
import com.sgbread.app.audio.AudioManager
import com.sgbread.app.audio.Sfx
import com.sgbread.app.audio.playActivityInstruction
import com.sgbread.app.data.BlendWord
import com.sgbread.app.data.LettersBank
import com.sgbread.app.data.Praise
import com.sgbread.app.ui.components.ActivityCompleteOverlay
import com.sgbread.app.ui.components.ActivityScaffold
import com.sgbread.app.ui.components.AnswerFeedback
import com.sgbread.app.ui.components.ChoiceState
import com.sgbread.app.ui.components.LetterChip
import com.sgbread.app.ui.components.PictureChoiceCard
import com.sgbread.app.ui.components.TwoPaneActivityBody
import com.sgbread.app.ui.components.activityLayoutMetrics
import com.sgbread.app.ui.theme.CreamWhite
import com.sgbread.app.ui.theme.SgbReadTheme
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun BlendReadScreen(audio: AudioManager?, onComplete: () -> Unit, onBack: () -> Unit) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val responsiveTextScale = (screenWidthDp / 360f).coerceIn(1f, 1.25f)

    // Freshly shuffled each time the screen is entered, not just once per app launch.
    val blendItems = remember { LettersBank.blendWords }
    val rounds = remember { blendItems.shuffled() }
    var roundIndex by remember { mutableStateOf(0) }
    var feedback by remember { mutableStateOf<AnswerFeedback>(AnswerFeedback.None) }
    var wrongWord by remember { mutableStateOf<String?>(null) }
    var correctWord by remember { mutableStateOf<String?>(null) }
    var roundLocked by remember { mutableStateOf(false) }
    var pendingPraise by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }

    val round = rounds[roundIndex]
    val choices = remember(roundIndex) {
        (listOf(round) + blendItems.filter { it.word != round.word }.shuffled().take(1)).shuffled()
    }

    fun speakPrompt() {
        val wordAudio = round.audio
        if (wordAudio != null) {
            audio?.playLettersThenRawWord(round.word, wordAudio, "blend-word:${round.word}", rate = 0.8f)
        } else {
            audio?.playLettersThenWord(round.word, rate = 0.8f)
        }
    }

    var hasIntroduced by remember { mutableStateOf(false) }
    LaunchedEffect(roundIndex) {
        wrongWord = null
        correctWord = null
        roundLocked = false
        if (!hasIntroduced) {
            hasIntroduced = true
            audio.playActivityInstruction(ActivityInstruction.BLEND_AND_READ) {
                speakPrompt()
            }
            return@LaunchedEffect
        }
        speakPrompt()
    }

    fun onPick(choice: BlendWord) {
        if (roundLocked) return
        // Every card speaks its own word on tap, whether or not it's the right answer --
        // lets the child explore/hear all the choices, not just the one they land on.
        audio?.stopPlayback()
        if (choice.word == round.word) {
            wrongWord = null
            correctWord = choice.word
            roundLocked = true
            audio.playBlendWord(choice, rate = 0.9f) {
                pendingPraise = true
            }
        } else {
            correctWord = null
            audio.playBlendWord(choice, rate = 0.9f) {
                audio?.playSfx(Sfx.INCORRECT)
                wrongWord = choice.word
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
            if (roundIndex == rounds.lastIndex) {
                audio?.playSfx(Sfx.HARVEST)
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
        title = "Blend and Read",
        onBack = onBack,
        onReplayInstructions = {
            audio.playActivityInstruction(ActivityInstruction.BLEND_AND_READ) {
                speakPrompt()
            }
        },
        feedback = feedback,
        audio = audio,
        blockInputDuringAudio = false,
        replayInstructionsEnabled = !roundLocked,
        titleTextScale = responsiveTextScale,
        confirmOnBack = roundIndex > 0 || correctWord != null
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
                    .padding(horizontal = metrics.horizontalPadding, vertical = metrics.verticalPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Word ${roundIndex + 1} of ${rounds.size}",
                    style = MaterialTheme.typography.bodyMedium.let { baseStyle ->
                        baseStyle.copy(
                            color = CreamWhite,
                            fontSize = baseStyle.fontSize * responsiveTextScale
                        )
                    }
                )
                Text(
                    "Click each letter sound, blend and read the word, then choose the correct picture.",
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
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(metrics.gridSpacing, Alignment.CenterHorizontally),
                            verticalArrangement = Arrangement.spacedBy(metrics.gridSpacing)
                        ) {
                            round.word.forEach { c ->
                                LetterChip(
                                    letter = c.uppercaseChar(),
                                    size = metrics.chipSize,
                                    enabled = !roundLocked,
                                    onClick = {
                                        audio?.stopPlayback()
                                        audio?.playLetterSound(c)
                                    }
                                )
                            }
                        }
                        Text(
                            round.word,
                            style = MaterialTheme.typography.headlineMedium.let { baseStyle ->
                                baseStyle.copy(
                                    color = CreamWhite,
                                    fontSize = baseStyle.fontSize * responsiveTextScale
                                )
                            },
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(top = metrics.gridSpacing)
                        )
                    },
                    choices = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(metrics.gridSpacing),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            choices.forEach { choice ->
                                PictureChoiceCard(
                                    icon = choice.icon,
                                    image = choice.image,
                                    label = null,
                                    imageSize = metrics.choiceImageSize,
                                    state = when (choice.word) {
                                        correctWord -> ChoiceState.CORRECT
                                        wrongWord -> ChoiceState.WRONG
                                        else -> ChoiceState.IDLE
                                    },
                                    enabled = !roundLocked,
                                    onClick = { onPick(choice) },
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

private fun AudioManager?.playBlendWord(
    word: BlendWord,
    rate: Float = 1f,
    onComplete: (() -> Unit)? = null
) {
    val wordAudio = word.audio
    if (wordAudio != null) {
        this?.playRawResource(wordAudio, "blend-word:${word.word}", onComplete)
    } else {
        this?.playWord(word.word, rate, onComplete)
    }
}

@Preview(device = "spec:width=360dp,height=800dp,orientation=portrait", showBackground = true)
@Composable
private fun BlendReadScreenPreview() {
    SgbReadTheme {
        BlendReadScreen(audio = null, onComplete = {}, onBack = {})
    }
}
