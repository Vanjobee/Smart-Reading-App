package com.sgbread.app.screens.module2

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
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
import com.sgbread.app.ui.theme.SgbReadTheme
import kotlinx.coroutines.delay

// Module 2 uses larger touch targets/text than the shared activity defaults.
private const val M2_SCALE = 1.25f
private const val CHOICE_COUNT = 6

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TapLetterScreen(audio: AudioManager, onComplete: () -> Unit, onBack: () -> Unit) {
    // Freshly shuffled each time the screen is entered, not just once per app launch.
    val rounds = remember { LettersBank.phonicsItems.shuffled().take(10) }
    var roundIndex by remember { mutableStateOf(0) }
    var feedback by remember { mutableStateOf<AnswerFeedback>(AnswerFeedback.None) }
    var wrongLetter by remember { mutableStateOf<Char?>(null) }
    var pendingPraise by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }

    val round = rounds[roundIndex]
    val choices = remember(roundIndex) {
        (listOf(round.letter) + LettersBank.phonicsItems
            .map { it.letter }
            .filter { it != round.letter }
            .distinct()
            .shuffled()
            .take(CHOICE_COUNT - 1))
            .distinct()
            .shuffled()
    }

    fun speakPrompt() = audio.playWord(round.word, rate = 0.85f)

    var hasIntroduced by remember { mutableStateOf(false) }
    LaunchedEffect(roundIndex) {
        wrongLetter = null
        if (!hasIntroduced) {
            hasIntroduced = true
            audio.playRecordedPrompt("What letter does this picture begin with?")
            delay(900)
        }
        speakPrompt()
    }

    fun onPick(letter: Char) {
        audio.stopPlayback()
        if (letter == round.letter) {
            audio.speakLetterThenWord(round.letter, round.word, rate = 0.9f) {
                pendingPraise = true
            }
        } else {
            audio.playSfx(Sfx.INCORRECT)
            audio.playLetterSound(letter)
            wrongLetter = letter
            feedback = AnswerFeedback.Incorrect(Praise.randomEncouragement())
        }
    }

    LaunchedEffect(pendingPraise) {
        if (pendingPraise) {
            delay(900)
            audio.playSfx(Sfx.CORRECT)
            feedback = AnswerFeedback.Correct(Praise.randomCorrect())
            pendingPraise = false
        }
    }

    LaunchedEffect(feedback) {
        val current = feedback
        if (current is AnswerFeedback.Correct) {
            delay(1200)
            feedback = AnswerFeedback.None
            if (roundIndex == rounds.lastIndex) {
                audio.playSfx(Sfx.HARVEST)
                finished = true
            } else {
                roundIndex += 1
            }
        } else if (current is AnswerFeedback.Incorrect) {
            delay(1000)
            feedback = AnswerFeedback.None
        }
    }

    ActivityScaffold(
        title = "Tap the Letter",
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
                Text("Round ${roundIndex + 1} of ${rounds.size}", style = MaterialTheme.typography.titleMedium)
                Text(
                    "What letter does this start with?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = metrics.spacing)
                )
                TwoPaneActivityBody(
                    metrics = metrics,
                    modifier = Modifier.weight(1f),
                    question = {
                        Image(
                            painter = painterResource(round.image),
                            contentDescription = round.word,
                            modifier = Modifier
                                .size(metrics.largePictureSize * M2_SCALE)
                                .clickable {
                                    audio.stopPlayback()
                                    audio.playWord(round.word, rate = 0.9f)
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
                                    state = if (wrongLetter == c) ChoiceState.WRONG else ChoiceState.IDLE,
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
        TapLetterScreen(audio = AudioManager.getInstance(LocalContext.current), onComplete = {}, onBack = {})
    }
}
