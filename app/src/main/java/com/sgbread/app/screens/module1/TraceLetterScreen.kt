package com.sgbread.app.screens.module1

import android.annotation.SuppressLint
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.sgbread.app.audio.AudioManager
import com.sgbread.app.audio.Sfx
import com.sgbread.app.data.FarmIconKey
import com.sgbread.app.data.LettersBank
import com.sgbread.app.data.PhonicsItem
import com.sgbread.app.ui.components.ActivityCompleteOverlay
import com.sgbread.app.ui.components.ActivityScaffold
import com.sgbread.app.ui.components.ResponsiveColumn
import com.sgbread.app.ui.components.activityLayoutMetrics
import com.sgbread.app.ui.icons.FarmIcon
import com.sgbread.app.ui.theme.CreamWhite
import com.sgbread.app.ui.theme.RiceGreenDark
import com.sgbread.app.ui.theme.SgbReadTheme
import com.sgbread.app.ui.theme.SoilBrown
import kotlinx.coroutines.delay

private const val SAMPLE_GRID = 18
// Requires most of the glyph's interior to be covered (not just a rough scribble)
// before the trace counts as complete and the word is revealed.
private const val COMPLETION_THRESHOLD = 0.9f
private const val LETTER_POPUP_HOLD_AFTER_AUDIO_MS = 450L

/** The traceable region of a glyph: its filled outline plus a grid of interior sample
 * points used to measure how much of the letter the child has actually covered. */
private data class GlyphMask(
    val outline: Path,
    val samplePoints: List<Offset>,
    val revealRadius: Float,
    val brushWidth: Float
)

private data class TraceLetterItem(
    val letter: Char,
    val word: String,
    val image: Int? = null,
    val icon: FarmIconKey? = null
)

private fun PhonicsItem.toTraceLetterItem(): TraceLetterItem =
    if (letter == 'R' && word == "rice") {
        TraceLetterItem(letter = 'R', word = "rat", icon = FarmIconKey.RAT)
    } else {
        TraceLetterItem(letter = letter, word = word, image = image)
    }

private fun buildGlyphMask(glyph: String, width: Float, height: Float): GlyphMask? {
    if (width <= 0f || height <= 0f) return null
    val minDim = minOf(width, height)
    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
        textSize = minDim * if (glyph in setOf("Mm", "Ww")) 0.52f else 0.62f
        textAlign = android.graphics.Paint.Align.LEFT
        isFakeBoldText = true
    }
    val outline = android.graphics.Path()
    paint.getTextPath(glyph, 0, glyph.length, 0f, 0f, outline)
    val bounds = android.graphics.RectF()
    outline.computeBounds(bounds, true)
    outline.offset(
        (width - bounds.width()) / 2f - bounds.left,
        (height - bounds.height()) / 2f - bounds.top
    )
    outline.computeBounds(bounds, true)
    val clipBounds = android.graphics.Region(0, 0, width.toInt().coerceAtLeast(1), height.toInt().coerceAtLeast(1))
    val region = android.graphics.Region().apply { setPath(outline, clipBounds) }

    val points = mutableListOf<Offset>()
    for (row in 0 until SAMPLE_GRID) {
        for (col in 0 until SAMPLE_GRID) {
            val x = bounds.left + (bounds.right - bounds.left) * (col + 0.5f) / SAMPLE_GRID
            val y = bounds.top + (bounds.bottom - bounds.top) * (row + 0.5f) / SAMPLE_GRID
            if (region.contains(x.toInt(), y.toInt())) points.add(Offset(x, y))
        }
    }
    if (points.isEmpty()) return null
    return GlyphMask(
        outline = outline.asComposePath(),
        samplePoints = points,
        revealRadius = minDim * 0.07f,
        brushWidth = minDim * 0.16f
    )
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun TraceLetterScreen(audio: AudioManager?, onComplete: () -> Unit, onBack: () -> Unit) {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp
    val responsiveTextScale = (screenWidthDp / 360f).coerceIn(1f, 1.25f)

    // Freshly shuffled each time the screen is entered, so replays don't always start on A-F.
    val traceLetters = remember { LettersBank.phonicsItems.map { it.toTraceLetterItem() }.shuffled().take(10) }
    var stepIndex by remember { mutableStateOf(0) }
    // Finished strokes plus the one currently being drawn, kept separate so lifting a
    // finger between strokes (e.g. the crossbar of "A") doesn't erase earlier ink.
    var strokes by remember { mutableStateOf(listOf<List<Offset>>()) }
    var currentStroke by remember { mutableStateOf(listOf<Offset>()) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var letterPopupVisible by remember { mutableStateOf(false) }
    var popupItem by remember { mutableStateOf(traceLetters.first()) }
    var completionAudioFinished by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }

    val totalSteps = traceLetters.size
    val item = traceLetters[stepIndex]
    val glyph = "${item.letter.uppercaseChar()}${item.letter.lowercaseChar()}"
    val popupGlyph = "${popupItem.letter.uppercaseChar()}${popupItem.letter.lowercaseChar()}"

    // Manual replay gives the recorded letter name without revealing the reward word.
    fun speakCurrent() {
        audio?.playLetterName(item.letter)
    }

    // Completion repeats the recorded letter name, then reveals its vocabulary word.
    fun speakCompletion() {
        completionAudioFinished = false
        audio?.speakLetterNameThenWord(item.letter, item.word, rate = 0.85f) {
            completionAudioFinished = true
        }
    }

    // Rebuilt whenever the letter or canvas size changes; supplies both the touch-hit
    // region (samplePoints) and the clip outline used to paint the color reveal.
    val glyphMask = remember(stepIndex, canvasSize) {
        buildGlyphMask(glyph, canvasSize.width.toFloat(), canvasSize.height.toFloat())
    }
    val revealed = remember(glyphMask) {
        mutableStateListOf<Boolean>().apply { repeat(glyphMask?.samplePoints?.size ?: 0) { add(false) } }
    }

    var hasIntroduced by remember { mutableStateOf(false) }

    LaunchedEffect(stepIndex) {
        strokes = emptyList()
        currentStroke = emptyList()
        letterPopupVisible = false
        completionAudioFinished = false
        if (!hasIntroduced) {
            hasIntroduced = true
            audio?.playRecordedPrompt("Trace Letter!") {
                audio.playLetterName(item.letter)
            }
            return@LaunchedEffect
        }
        // Only the letter's name is given up front — the word is revealed after
        // the child finishes tracing, so it doesn't give the answer away early.
        audio?.playLetterName(item.letter)
    }

    LaunchedEffect(completionAudioFinished) {
        if (completionAudioFinished) {
            delay(LETTER_POPUP_HOLD_AFTER_AUDIO_MS)
            letterPopupVisible = false
            completionAudioFinished = false
            if (stepIndex == totalSteps - 1) {
                audio?.playSfx(Sfx.HARVEST)
                finished = true
            } else {
                stepIndex += 1
            }
        }
    }

    ActivityScaffold(
        title = "Letter Trace",
        onBack = onBack,
        onReplayInstructions = { speakCurrent() },
        audio = audio,
        blockInputDuringAudio = false,
        titleTextScale = responsiveTextScale,
        confirmOnBack = stepIndex > 0 || strokes.isNotEmpty() || currentStroke.isNotEmpty()
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
                verticalArrangement = Arrangement.spacedBy(if (metrics.compactHeight) 2.dp else 8.dp),
                verticalPadding = if (metrics.compactHeight) 2.dp else 8.dp
            ) {
                Text(
                    "Letter ${stepIndex + 1} of ${traceLetters.size} — trace $glyph",
                    style = (
                        if (metrics.compactHeight) {
                            MaterialTheme.typography.labelMedium
                        } else {
                            MaterialTheme.typography.bodyMedium
                        }
                    ).let { baseStyle ->
                        baseStyle.copy(
                            color = CreamWhite,
                            fontSize = baseStyle.fontSize * responsiveTextScale
                        )
                    },
                    modifier = Modifier.padding(bottom = 0.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (metrics.compactWidth) 0.94f else 0.82f)
                        .height(if (metrics.compactHeight) 240.dp else 340.dp)
                        .padding(bottom = 0.dp)
                ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged { canvasSize = it }
                        .pointerInput(stepIndex, canvasSize) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    if (letterPopupVisible) return@detectDragGestures
                                    audio?.stopPlayback()
                                    audio?.startWritingSound()
                                    if (strokes.isEmpty() && currentStroke.isEmpty()) audio?.playLetterName(item.letter)
                                    currentStroke = listOf(offset)
                                },
                                onDrag = { change, _ ->
                                    val point = change.position
                                    currentStroke = currentStroke + point
                                    val mask = glyphMask
                                    if (mask != null && !letterPopupVisible && mask.samplePoints.isNotEmpty()) {
                                        var newlyRevealed = false
                                        for (i in mask.samplePoints.indices) {
                                            if (!revealed[i] && (mask.samplePoints[i] - point).getDistance() < mask.revealRadius) {
                                                revealed[i] = true
                                                newlyRevealed = true
                                            }
                                        }
                                        if (newlyRevealed) {
                                            val coverage = revealed.count { it } / mask.samplePoints.size.toFloat()
                                            if (coverage >= COMPLETION_THRESHOLD) {
                                                popupItem = item
                                                audio?.playSfx(Sfx.CORRECT)
                                                speakCompletion()
                                                letterPopupVisible = true
                                            }
                                        }
                                    }
                                },
                                onDragEnd = {
                                    audio?.stopWritingSound()
                                    if (currentStroke.isNotEmpty()) {
                                        strokes = strokes + listOf(currentStroke)
                                        currentStroke = emptyList()
                                    }
                                },
                                onDragCancel = {
                                    audio?.stopWritingSound()
                                    if (currentStroke.isNotEmpty()) {
                                        strokes = strokes + listOf(currentStroke)
                                        currentStroke = emptyList()
                                    }
                                }
                            )
                        }
                ) {
                    drawRoundRect(CreamWhite, cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f, 24f))
                    val mask = glyphMask
                    if (mask != null) {
                        drawPath(
                            mask.outline,
                            SoilBrown.copy(alpha = 0.22f)
                        )
                        clipPath(mask.outline) {
                            (strokes + listOf(currentStroke)).forEach { stroke ->
                                if (stroke.size > 1) {
                                    val revealPath = Path().apply {
                                        moveTo(stroke.first().x, stroke.first().y)
                                        stroke.drop(1).forEach { lineTo(it.x, it.y) }
                                    }
                                    drawPath(
                                        revealPath,
                                        RiceGreenDark,
                                        style = Stroke(width = mask.brushWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                    )
                                }
                            }
                        }
                    }
                }

                }
            }

            androidx.compose.animation.AnimatedVisibility(
                    visible = letterPopupVisible,
                    enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)),
                    exit = scaleOut(),
                    modifier = Modifier.matchParentSize()
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color(0x99000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier
                                .background(CreamWhite, RoundedCornerShape(28.dp))
                                .padding(if (metrics.compactHeight) 22.dp else 28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val rewardImageSize =
                                if (metrics.compactHeight) {
                                    metrics.largePictureSize
                                } else {
                                    metrics.largePictureSize * 1.15f
                                }
                            val rewardImage = popupItem.image
                            val rewardIcon = popupItem.icon
                            if (rewardImage != null) {
                                Image(
                                    painter = painterResource(rewardImage),
                                    contentDescription = popupItem.word,
                                    modifier = Modifier.size(rewardImageSize),
                                    contentScale = ContentScale.Fit
                                )
                            } else if (rewardIcon != null) {
                                FarmIcon(
                                    rewardIcon,
                                    modifier = Modifier.size(rewardImageSize),
                                    background = null
                                )
                            }
                            Text(
                                "\"$popupGlyph\" is for \"${popupItem.word}\"",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = SoilBrown,
                                modifier = Modifier.padding(top = metrics.gridSpacing)
                            )
                        }
                    }
                }
        }

        if (finished) {
            ActivityCompleteOverlay(onContinue = onComplete)
        }
    }
}

@Preview(device = "spec:width=360dp,height=800dp,orientation=portrait", showBackground = true)
@Composable
private fun TraceLetterScreenPreview() {
    SgbReadTheme {
        TraceLetterScreen(audio = null, onComplete = {}, onBack = {})
    }
}
