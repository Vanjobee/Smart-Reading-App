package com.sgbread.app.screens.module4

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sgbread.app.audio.AudioManager
import com.sgbread.app.audio.Sfx
import com.sgbread.app.data.LettersBank
import com.sgbread.app.data.Praise
import com.sgbread.app.ui.components.ActivityCompleteOverlay
import com.sgbread.app.ui.components.ActivityScaffold
import com.sgbread.app.ui.components.AnswerFeedback
import com.sgbread.app.ui.components.ChoiceState
import com.sgbread.app.ui.components.TwoPaneActivityBody
import com.sgbread.app.ui.components.activityLayoutMetrics
import com.sgbread.app.ui.icons.FarmIcon
import com.sgbread.app.ui.theme.CorrectGreen
import com.sgbread.app.ui.theme.CreamWhite
import com.sgbread.app.ui.theme.IncorrectRed
import com.sgbread.app.ui.theme.SgbReadTheme
import com.sgbread.app.ui.theme.SoilBrown
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PictureWordMatchScreen(audio: AudioManager, onComplete: () -> Unit, onBack: () -> Unit) {
    // Freshly shuffled each time the screen is entered, not just once per app launch.
    val rounds = remember { LettersBank.patternWords.distinctBy { it.word }.shuffled().take(10) }
    var roundIndex by remember { mutableStateOf(0) }
    var feedback by remember { mutableStateOf<AnswerFeedback>(AnswerFeedback.None) }
    var wrongWord by remember { mutableStateOf<String?>(null) }
    var pendingPraise by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }

    val round = rounds[roundIndex]
    val choices = remember(roundIndex) {
        val distractors = rounds.filter { it.word != round.word }.shuffled().take(1).map { it.word }
        (distractors + round.word).shuffled()
    }

    fun speakWord() = audio.playWord(round.word, rate = 0.85f)

    var hasIntroduced by remember { mutableStateOf(false) }
    LaunchedEffect(roundIndex) {
        wrongWord = null
        if (!hasIntroduced) {
            hasIntroduced = true
            audio.playRecordedPrompt("Tap the picture!")
            delay(900)
        }
        speakWord()
    }

    fun onPick(word: String) {
        audio.stopPlayback()
        if (word == round.word) {
            audio.playWord(round.word, rate = 0.9f) {
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
        title = "Picture-to-Word Match",
        onBack = onBack,
        onReplayInstructions = { speakWord() },
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
                Text("Word ${roundIndex + 1} of ${rounds.size}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Tap the picture to hear it, then pick the spelling",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = metrics.spacing)
                )
                TwoPaneActivityBody(
                    metrics = metrics,
                    modifier = Modifier.weight(1f),
                    question = {
                        if (round.image != null) {
                            Image(
                                painter = painterResource(round.image),
                                contentDescription = round.word,
                                modifier = Modifier
                                    .size(metrics.largePictureSize)
                                    .clickable {
                                        audio.stopPlayback()
                                        speakWord()
                                    },
                                contentScale = ContentScale.Fit
                            )
                        } else if (round.icon != null) {
                            FarmIcon(
                                round.icon,
                                modifier = Modifier
                                    .size(metrics.largePictureSize)
                                    .clickable {
                                        audio.stopPlayback()
                                        speakWord()
                                    }
                            )
                        }
                    },
                    choices = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(metrics.gridSpacing),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            choices.forEach { word ->
                                SpellingChoiceBox(
                                    word = word,
                                    state = when {
                                        wrongWord == word -> ChoiceState.WRONG
                                        word == round.word && feedback is AnswerFeedback.Correct -> ChoiceState.CORRECT
                                        else -> ChoiceState.IDLE
                                    },
                                    onClick = { onPick(word) },
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

@Composable
private fun SpellingChoiceBox(
    word: String,
    state: ChoiceState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = when (state) {
        ChoiceState.CORRECT -> CorrectGreen
        ChoiceState.WRONG -> IncorrectRed
        ChoiceState.SELECTED -> CorrectGreen
        ChoiceState.IDLE -> Color(0x33000000)
    }
    BoxWithConstraints(
        modifier = modifier
            .background(CreamWhite, RoundedCornerShape(10.dp))
            .border(3.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            word,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = SoilBrown
        )
    }
}

@Preview(device = "spec:width=360dp,height=800dp,orientation=portrait", showBackground = true)
@Composable
private fun PictureWordMatchScreenPreview() {
    SgbReadTheme {
        PictureWordMatchScreen(
            audio = AudioManager.getInstance(LocalContext.current),
            onComplete = {},
            onBack = {}
        )
    }
}
