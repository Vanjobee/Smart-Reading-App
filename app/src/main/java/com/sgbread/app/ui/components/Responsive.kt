package com.sgbread.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import kotlin.math.roundToInt

data class ActivityLayoutMetrics(
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
    val spacing: Dp,
    val chipSize: Dp,
    val choiceChipSize: Dp,
    val pictureSize: Dp,
    val largePictureSize: Dp,
    val choiceImageSize: Dp,
    val gridSpacing: Dp,
    val feedbackClearance: Dp,
    val compactWidth: Boolean,
    val compactHeight: Boolean
)

@Composable
fun activityLayoutMetrics(maxWidth: Dp, maxHeight: Dp): ActivityLayoutMetrics {
    val compactWidth = maxWidth < 360.dp
    val compactHeight = maxHeight < 560.dp
    val veryCompactHeight = maxHeight < 480.dp

    return ActivityLayoutMetrics(
        horizontalPadding = if (compactWidth) 12.dp else 20.dp,
        verticalPadding = if (compactHeight) 6.dp else 12.dp,
        spacing = if (compactHeight) 8.dp else 14.dp,
        chipSize = when {
            compactWidth || veryCompactHeight -> 52.dp
            compactHeight -> 56.dp
            else -> 64.dp
        },
        choiceChipSize = when {
            compactWidth || veryCompactHeight -> 72.dp
            compactHeight -> 80.dp
            else -> 92.dp
        },
        pictureSize = when {
            compactWidth || veryCompactHeight -> 104.dp
            compactHeight -> 120.dp
            else -> 140.dp
        },
        largePictureSize = when {
            compactWidth || veryCompactHeight -> 132.dp
            compactHeight -> 150.dp
            else -> 170.dp
        },
        choiceImageSize = when {
            compactWidth || veryCompactHeight -> 92.dp
            compactHeight -> 104.dp
            else -> 120.dp
        },
        gridSpacing = if (compactWidth || compactHeight) 6.dp else 10.dp,
        feedbackClearance = if (compactHeight) 56.dp else 88.dp,
        compactWidth = compactWidth,
        compactHeight = compactHeight
    )
}

/**
 * A scrollable column that prevents content clipping on small screens and
 * handles vertical padding automatically based on the activity metrics.
 */
@Composable
fun ResponsiveColumn(
    metrics: ActivityLayoutMetrics,
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(
        metrics.spacing,
        Alignment.CenterVertically
    ),
    verticalPadding: Dp = metrics.verticalPadding,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = metrics.horizontalPadding, vertical = verticalPadding),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
        content = content
    )
}

/**
 * A text component that attempts to fit within its container by using a
 * smaller font size if necessary. For now, it uses a simple scale-down approach
 * or just ensures standard 'sp' scaling is respected.
 */
@Composable
fun AutoSizeText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    maxLines: Int = 2,
    textAlign: TextAlign = TextAlign.Center,
    minFontSize: Dp = 12.dp
) {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    var currentFontSize by remember(text, style, maxLines) { mutableStateOf(style.fontSize) }

    BoxWithConstraints(modifier = modifier) {
        val maxWidthPx = with(density) { maxWidth.toPx().roundToInt() }
        val maxHeightPx = with(density) { maxHeight.toPx().roundToInt() }
        val minFontSizeSp = with(density) { minFontSize.toSp() }

        LaunchedEffect(text, style, maxWidthPx, maxHeightPx, maxLines) {
            var size = style.fontSize
            while (size > minFontSizeSp) {
                val layoutResult = textMeasurer.measure(
                    text = AnnotatedString(text),
                    style = style.copy(fontSize = size),
                    maxLines = maxLines,
                    overflow = TextOverflow.Ellipsis,
                    constraints = Constraints(maxWidth = maxWidthPx)
                )
                val fitsWidth = layoutResult.size.width <= maxWidthPx
                val fitsHeight = layoutResult.size.height <= maxHeightPx.coerceAtLeast(1)
                if (fitsWidth && fitsHeight) {
                    currentFontSize = size
                    break
                }
                size = size * 0.92f
            }
            if (currentFontSize < minFontSizeSp) currentFontSize = minFontSizeSp
        }

        Text(
            text = text,
            modifier = modifier,
            style = style.copy(fontSize = currentFontSize),
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            textAlign = textAlign
        )
    }
}

/**
 * Splits an activity's body into a "question" pane on top and a "choices" pane below --
 * the app is portrait-only now, so vertical space is the abundant dimension and a
 * side-by-side split would squeeze both panes into too little width to be usable.
 */
@Composable
fun TwoPaneActivityBody(
    metrics: ActivityLayoutMetrics,
    modifier: Modifier = Modifier,
    question: @Composable ColumnScope.() -> Unit,
    choices: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(metrics.spacing, Alignment.CenterVertically)
    ) {
        question()
        choices()
    }
}
