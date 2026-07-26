package com.sgbread.app.screens.module3

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sgbread.app.audio.AudioManager
import com.sgbread.app.audio.Sfx
import com.sgbread.app.data.BlendWord
import com.sgbread.app.data.LettersBank
import com.sgbread.app.data.Praise
import com.sgbread.app.ui.components.ActivityCompleteOverlay
import com.sgbread.app.ui.components.ActivityScaffold
import com.sgbread.app.ui.components.AnswerFeedback
import com.sgbread.app.ui.components.ChoiceState
import com.sgbread.app.ui.components.activityLayoutMetrics
import com.sgbread.app.ui.icons.FarmIcon
import com.sgbread.app.ui.theme.CorrectGreen
import com.sgbread.app.ui.theme.CreamWhite
import com.sgbread.app.ui.theme.IncorrectRed
import com.sgbread.app.ui.theme.RiceGreenDark
import com.sgbread.app.ui.theme.SgbReadTheme
import com.sgbread.app.ui.theme.SoilBrown
import kotlinx.coroutines.delay

private val BLENDING_MATCH_EXCLUDED_WORDS = setOf("rice", "goat")

@Composable
fun BlendingMatchScreen(audio: AudioManager, onComplete: () -> Unit, onBack: () -> Unit) {
    val matchWords = remember {
        LettersBank.blendWords.filter { it.word.length == 3 && it.word !in BLENDING_MATCH_EXCLUDED_WORDS }
    }
    val rounds = remember { matchWords.shuffled().take(10) }
    var roundIndex by remember { mutableStateOf(0) }
    var feedback by remember { mutableStateOf<AnswerFeedback>(AnswerFeedback.None) }
    var wrongWord by remember { mutableStateOf<String?>(null) }
    var correctWord by remember { mutableStateOf<String?>(null) }
    var roundLocked by remember { mutableStateOf(false) }
    var pendingPraise by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }

    val round = rounds[roundIndex]
    val choices = remember(roundIndex) {
        val distractors = matchWords
            .filter { it.word != round.word }
            .shuffled()
            .take(2)
        (distractors + round).shuffled()
    }

    fun speakPrompt() = audio.playWord(round.word, rate = 0.9f)

    var hasIntroduced by remember { mutableStateOf(false) }
    LaunchedEffect(roundIndex) {
        wrongWord = null
        correctWord = null
        roundLocked = false
        if (!hasIntroduced) {
            hasIntroduced = true
            audio.playRecordedPrompt("Tap the matching word!") {
                speakPrompt()
            }
            return@LaunchedEffect
        }
        speakPrompt()
    }

    fun onPick(choice: BlendWord) {
        if (roundLocked) return
        audio.stopPlayback()
        if (choice.word == round.word) {
            correctWord = choice.word
            roundLocked = true
            audio.playWord(choice.word, rate = 0.9f) {
                pendingPraise = true
            }
        } else {
            wrongWord = choice.word
            audio.playWord(choice.word, rate = 0.9f) {
                audio.playSfx(Sfx.INCORRECT)
                feedback = AnswerFeedback.Incorrect(Praise.randomEncouragement())
            }
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
            if (roundIndex == rounds.lastIndex) {
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
        title = "Blending Match",
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
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Look at the picture, then tap the matching word",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = metrics.spacing)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(metrics.spacing, Alignment.CenterVertically)
                    ) {
                        BlendingPictureCard(
                            word = round,
                            imageSize = metrics.largePictureSize,
                            onClick = {
                                audio.stopPlayback()
                                speakPrompt()
                            }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(metrics.gridSpacing, Alignment.CenterHorizontally),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            choices.forEach { choice ->
                                BlendingWordChoice(
                                    word = choice.word,
                                    state = choiceState(choice.word, correctWord, wrongWord),
                                    enabled = !roundLocked,
                                    onClick = { onPick(choice) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (finished) {
            ActivityCompleteOverlay(onContinue = onComplete)
        }
    }
}

private fun choiceState(word: String, correctWord: String?, wrongWord: String?): ChoiceState = when (word) {
    correctWord -> ChoiceState.CORRECT
    wrongWord -> ChoiceState.WRONG
    else -> ChoiceState.IDLE
}

@Composable
private fun BlendingPictureCard(
    word: BlendWord,
    imageSize: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(28.dp))
            .background(CreamWhite.copy(alpha = 0.96f), RoundedCornerShape(28.dp))
            .border(4.dp, RiceGreenDark.copy(alpha = 0.7f), RoundedCornerShape(28.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        if (word.image != null) {
            Image(
                painter = painterResource(word.image),
                contentDescription = word.word,
                modifier = Modifier.size(imageSize),
                contentScale = ContentScale.Fit
            )
        } else if (word.icon != null) {
            FarmIcon(word.icon, modifier = Modifier.size(imageSize), background = null)
        }
    }
}

@Composable
private fun BlendingWordChoice(
    word: String,
    state: ChoiceState,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = when (state) {
        ChoiceState.CORRECT -> CorrectGreen
        ChoiceState.WRONG -> IncorrectRed
        ChoiceState.SELECTED -> RiceGreenDark
        ChoiceState.IDLE -> Color(0x408B5E34)
    }
    val backgroundColor = when (state) {
        ChoiceState.CORRECT -> Color(0xFFE8F7DF)
        ChoiceState.WRONG -> Color(0xFFFFE1DF)
        ChoiceState.SELECTED -> Color(0xFFFFF1C2)
        ChoiceState.IDLE -> CreamWhite.copy(alpha = 0.96f)
    }
    Box(
        modifier = modifier
            .shadow(if (state == ChoiceState.IDLE) 4.dp else 8.dp, RoundedCornerShape(18.dp))
            .background(backgroundColor, RoundedCornerShape(18.dp))
            .border(3.dp, borderColor, RoundedCornerShape(18.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            word,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = SoilBrown,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(device = "spec:width=360dp,height=800dp,orientation=portrait", showBackground = true)
@Composable
private fun BlendingMatchScreenPreview() {
    SgbReadTheme {
        BlendingMatchScreen(
            audio = AudioManager.getInstance(LocalContext.current),
            onComplete = {},
            onBack = {}
        )
    }
}
