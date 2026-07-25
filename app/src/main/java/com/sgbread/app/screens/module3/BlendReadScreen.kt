package com.sgbread.app.screens.module3

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import com.sgbread.app.ui.components.LetterChip
import com.sgbread.app.ui.components.PictureChoiceCard
import com.sgbread.app.ui.components.TwoPaneActivityBody
import com.sgbread.app.ui.components.activityLayoutMetrics
import com.sgbread.app.ui.theme.SgbReadTheme
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BlendReadScreen(audio: AudioManager, onComplete: () -> Unit, onBack: () -> Unit) {
    // Freshly shuffled each time the screen is entered, not just once per app launch.
    val rounds = remember { LettersBank.blendWords.shuffled().take(10) }
    var roundIndex by remember { mutableStateOf(0) }
    var feedback by remember { mutableStateOf<AnswerFeedback>(AnswerFeedback.None) }
    var wrongWord by remember { mutableStateOf<String?>(null) }
    var pendingPraise by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }

    val round = rounds[roundIndex]
    val choices = remember(roundIndex) {
        (listOf(round) + LettersBank.blendWords.filter { it.word != round.word }.shuffled().take(1)).shuffled()
    }

    fun speakPrompt() {
        audio.playLettersThenWord(round.word, rate = 0.8f)
    }

    var hasIntroduced by remember { mutableStateOf(false) }
    LaunchedEffect(roundIndex) {
        wrongWord = null
        if (!hasIntroduced) {
            hasIntroduced = true
            audio.playRecordedPrompt("Listen to the sounds blend them!")
            delay(900)
        }
        speakPrompt()
    }

    fun onPick(choice: BlendWord) {
        // Every card speaks its own word on tap, whether or not it's the right answer --
        // lets the child explore/hear all the choices, not just the one they land on.
        audio.playWord(choice.word, rate = 0.9f)
        if (choice.word == round.word) {
            pendingPraise = true
        } else {
            audio.playSfx(Sfx.INCORRECT)
            wrongWord = choice.word
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
        title = "Blend and Read",
        onBack = onBack,
        onReplayInstructions = { speakPrompt() },
        feedback = feedback,
        audio = audio
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
                    "Tap each letter to hear it, then find the picture",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = metrics.spacing)
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
                                    letter = c,
                                    size = metrics.chipSize,
                                    onClick = { audio.speakLetterThenWord(c, round.word, rate = 0.85f) }
                                )
                            }
                        }
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
                                    state = if (wrongWord == choice.word) ChoiceState.WRONG else ChoiceState.IDLE,
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

@Preview(device = "spec:width=360dp,height=800dp,orientation=portrait", showBackground = true)
@Composable
private fun BlendReadScreenPreview() {
    SgbReadTheme {
        BlendReadScreen(audio = AudioManager.getInstance(LocalContext.current), onComplete = {}, onBack = {})
    }
}
