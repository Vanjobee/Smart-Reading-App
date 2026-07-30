package com.sgbread.app.screens.module4

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sgbread.app.audio.ActivityInstruction
import com.sgbread.app.audio.AudioManager
import com.sgbread.app.audio.Sfx
import com.sgbread.app.audio.playActivityInstruction
import com.sgbread.app.data.LettersBank
import com.sgbread.app.data.PatternWord
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

private val DIGRAPH_PATTERNS = setOf("CH", "SH", "TH", "CK", "NG", "PH", "WH")
private const val DIGRAPH_CHOICE_COUNT = 4

@OptIn(ExperimentalLayoutApi::class)
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun DigraphBuildScreen(audio: AudioManager?, onComplete: () -> Unit, onBack: () -> Unit) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val responsiveTextScale = (screenWidthDp / 360f).coerceIn(1f, 1.25f)

    val rounds = remember {
        LettersBank.patternWords
            .filter { it.pattern in DIGRAPH_PATTERNS }
            .filter { it.image != null && it.audio != null }
            .distinctBy { it.word }
            .shuffled()
    }
    var roundIndex by remember { mutableStateOf(0) }
    var feedback by remember { mutableStateOf<AnswerFeedback>(AnswerFeedback.None) }
    var wrongPattern by remember { mutableStateOf<String?>(null) }
    var correctPattern by remember { mutableStateOf<String?>(null) }
    var roundLocked by remember { mutableStateOf(false) }
    var pendingPraise by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }
    var hasIntroduced by remember { mutableStateOf(false) }

    val round = rounds[roundIndex]
    val patternChoices = remember(roundIndex) {
        val distractors = DIGRAPH_PATTERNS
            .filter { it != round.pattern }
            .shuffled()
            .take(DIGRAPH_CHOICE_COUNT - 1)
        (distractors + round.pattern).shuffled()
    }

    fun speakPicture() = audio.playPatternWord(round, rate = 0.9f)

    LaunchedEffect(roundIndex) {
        wrongPattern = null
        correctPattern = null
        roundLocked = false
        if (!hasIntroduced) {
            hasIntroduced = true
            audio.playActivityInstruction(ActivityInstruction.DIGRAPH_SOUND) {
                speakPicture()
            }
            return@LaunchedEffect
        }
        speakPicture()
    }

    fun onPick(pattern: String) {
        if (roundLocked) return
        roundLocked = true
        audio?.stopPlayback()
        audio.playDigraphSound(pattern)
        if (pattern == round.pattern) {
            correctPattern = pattern
            pendingPraise = true
        } else {
            wrongPattern = pattern
            feedback = AnswerFeedback.Incorrect(Praise.randomEncouragement())
        }
    }

    LaunchedEffect(pendingPraise) {
        if (pendingPraise) {
            delay(600)
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
            wrongPattern = null
            roundLocked = false
        }
    }

    ActivityScaffold(
        title = "Digraph Sound",
        onBack = onBack,
        onReplayInstructions = {
            audio.playActivityInstruction(ActivityInstruction.DIGRAPH_SOUND) {
                speakPicture()
            }
        },
        feedback = feedback,
        audio = audio,
        blockInputDuringAudio = false,
        replayInstructionsEnabled = !roundLocked,
        titleTextScale = responsiveTextScale,
        confirmOnBack = roundIndex > 0 || correctPattern != null
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
                    style = MaterialTheme.typography.bodyMedium.let { baseStyle ->
                        baseStyle.copy(
                            color = CreamWhite,
                            fontSize = baseStyle.fontSize * responsiveTextScale
                        )
                    }
                )
                Text(
                    "Click the picture and listen to the word. Choose the correct digraph sound.",
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

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(metrics.spacing, Alignment.CenterVertically)
                ) {
                    DigraphPictureCard(
                        word = round,
                        imageSize = metrics.largePictureSize * responsiveImageScale,
                        enabled = !roundLocked,
                        onClick = {
                            audio?.stopPlayback()
                            speakPicture()
                        }
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(metrics.gridSpacing, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(metrics.gridSpacing)
                    ) {
                        patternChoices.forEach { pattern ->
                            DigraphSoundChoice(
                                pattern = pattern,
                                state = when (pattern) {
                                    correctPattern -> ChoiceState.CORRECT
                                    wrongPattern -> ChoiceState.WRONG
                                    else -> ChoiceState.IDLE
                                },
                                enabled = !roundLocked,
                                onClick = { onPick(pattern) }
                            )
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

@Composable
private fun DigraphPictureCard(
    word: PatternWord,
    imageSize: androidx.compose.ui.unit.Dp,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .shadow(8.dp, RoundedCornerShape(30.dp))
            .background(CreamWhite.copy(alpha = 0.96f), RoundedCornerShape(30.dp))
            .border(4.dp, RiceGreenDark.copy(alpha = 0.7f), RoundedCornerShape(30.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
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
        Text(
            word.word,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = SoilBrown,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun DigraphSoundChoice(
    pattern: String,
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
            .size(92.dp)
            .shadow(if (state == ChoiceState.IDLE) 4.dp else 8.dp, RoundedCornerShape(22.dp))
            .background(backgroundColor, RoundedCornerShape(22.dp))
            .border(3.dp, borderColor, RoundedCornerShape(22.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            pattern,
            fontSize = 34.sp,
            fontWeight = FontWeight.ExtraBold,
            color = SoilBrown,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(device = "spec:width=360dp,height=800dp,orientation=portrait", showBackground = true)
@Composable
private fun DigraphBuildScreenPreview() {
    SgbReadTheme {
        DigraphBuildScreen(
            audio = null,
            onComplete = {},
            onBack = {}
        )
    }
}
