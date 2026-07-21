package com.sgbread.app.screens.module1

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sgbread.app.audio.AudioManager
import com.sgbread.app.audio.Sfx
import com.sgbread.app.data.LettersBank
import com.sgbread.app.ui.components.ActivityCompleteOverlay
import com.sgbread.app.ui.components.ActivityScaffold
import com.sgbread.app.ui.components.AnswerFeedback
import com.sgbread.app.ui.theme.CreamWhite
import com.sgbread.app.ui.theme.RiceGreenDark
import com.sgbread.app.ui.theme.SoilBrown
import kotlinx.coroutines.delay

private val traceLetters = LettersBank.phonicsItems.take(6) // A-F, farm words already attached

@Composable
fun TraceLetterScreen(audio: AudioManager, onComplete: () -> Unit, onBack: () -> Unit) {
    var stepIndex by remember { mutableStateOf(0) } // 0..(letters.size*2 - 1)
    var pathPoints by remember { mutableStateOf(listOf<Offset>()) }
    var feedback by remember { mutableStateOf<AnswerFeedback>(AnswerFeedback.None) }
    var finished by remember { mutableStateOf(false) }

    val totalSteps = traceLetters.size * 2
    val item = traceLetters[stepIndex / 2]
    val isUpper = stepIndex % 2 == 0
    val glyph = if (isUpper) item.letter.uppercaseChar() else item.letter.lowercaseChar()

    fun speakCurrent() {
        audio.speak("${item.letter}. ${item.word}.", rate = 0.85f)
    }

    LaunchedEffect(stepIndex) {
        pathPoints = emptyList()
        speakCurrent()
    }

    LaunchedEffect(feedback) {
        if (feedback is AnswerFeedback.Correct) {
            delay(1100)
            feedback = AnswerFeedback.None
            if (stepIndex == totalSteps - 1) {
                audio.playSfx(Sfx.HARVEST)
                finished = true
            } else {
                stepIndex += 1
            }
        } else if (feedback is AnswerFeedback.Incorrect) {
            delay(1000)
            feedback = AnswerFeedback.None
        }
    }

    ActivityScaffold(
        title = "Trace the Letter",
        onBack = onBack,
        onReplayInstructions = { speakCurrent() },
        feedback = feedback
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Trace the letter with your finger",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Letter ${stepIndex / 2 + 1} of ${traceLetters.size} — ${if (isUpper) "Uppercase" else "Lowercase"}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Box(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .padding(12.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(stepIndex) {
                            val minDim = minOf(size.width, size.height).toFloat()
                            detectDragGestures(
                                onDragStart = { offset -> pathPoints = listOf(offset) },
                                onDrag = { change, _ -> pathPoints = pathPoints + change.position },
                                onDragEnd = {
                                    val points = pathPoints
                                    if (feedback is AnswerFeedback.None && points.size > 6) {
                                        var traveled = 0f
                                        for (i in 1 until points.size) {
                                            traveled += (points[i] - points[i - 1]).getDistance()
                                        }
                                        val spanX = points.maxOf { it.x } - points.minOf { it.x }
                                        val spanY = points.maxOf { it.y } - points.minOf { it.y }
                                        val coversLetter = traveled >= minDim * 0.8f &&
                                            spanX >= minDim * 0.25f &&
                                            spanY >= minDim * 0.3f
                                        if (coversLetter) {
                                            audio.playSfx(Sfx.CORRECT)
                                            feedback = AnswerFeedback.Correct("Great tracing!")
                                        } else {
                                            audio.playSfx(Sfx.INCORRECT)
                                            feedback = AnswerFeedback.Incorrect("Trace the whole letter!")
                                            pathPoints = emptyList()
                                        }
                                    }
                                }
                            )
                        }
                ) {
                    drawRoundRect(CreamWhite, cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f, 24f))
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val canvasMinDim = size.minDimension
                    val nativeCanvas = drawContext.canvas.nativeCanvas
                    val guidePaint = Paint().asFrameworkPaint().apply {
                        isAntiAlias = true
                        textSize = canvasMinDim * 0.7f
                        color = android.graphics.Color.argb(60, 74, 52, 35)
                        textAlign = android.graphics.Paint.Align.CENTER
                        isFakeBoldText = true
                    }
                    nativeCanvas.drawText(glyph.toString(), canvasWidth / 2f, canvasHeight * 0.72f, guidePaint)
                    if (pathPoints.size > 1) {
                        val tracePath = Path().apply {
                            moveTo(pathPoints.first().x, pathPoints.first().y)
                            pathPoints.drop(1).forEach { lineTo(it.x, it.y) }
                        }
                        drawPath(tracePath, RiceGreenDark, style = Stroke(width = 18f, cap = StrokeCap.Round))
                    }
                }
            }
            Text(
                "Say it: \"${item.letter}\" as in \"${item.word}\"",
                style = MaterialTheme.typography.bodyLarge,
                color = SoilBrown,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (finished) {
            ActivityCompleteOverlay(onContinue = onComplete)
        }
    }
}
