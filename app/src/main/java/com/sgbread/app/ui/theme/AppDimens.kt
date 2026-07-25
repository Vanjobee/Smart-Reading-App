package com.sgbread.app.ui.theme

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Layout tokens for activity screens. [Landscape] is used only when the screen is both
 * wider than it is tall and short on vertical room (typical mobile landscape, < 480dp
 * height): pictures and text scale up to use the abundant horizontal space instead of
 * staying phone-portrait-sized and clipping. Every other configuration uses [Default]. */
data class AppDimens(
    val horizontalPadding: Dp,
    val verticalPadding: Dp,
    val spacing: Dp,
    val chipSize: Dp,
    val pictureSize: Dp,
    val largePictureSize: Dp,
    val choiceImageSize: Dp,
    val gridSpacing: Dp,
    val feedbackClearance: Dp,
    val isLandscape: Boolean,
    val compactHeight: Boolean
) {
    companion object {
        val Default = AppDimens(
            horizontalPadding = 20.dp,
            verticalPadding = 12.dp,
            spacing = 14.dp,
            chipSize = 64.dp,
            pictureSize = 140.dp,
            largePictureSize = 170.dp,
            choiceImageSize = 88.dp,
            gridSpacing = 10.dp,
            feedbackClearance = 88.dp,
            isLandscape = false,
            compactHeight = false
        )

        val Landscape = AppDimens(
            horizontalPadding = 24.dp,
            verticalPadding = 8.dp,
            spacing = 10.dp,
            chipSize = 60.dp,
            pictureSize = 180.dp,
            largePictureSize = 220.dp,
            choiceImageSize = 96.dp,
            gridSpacing = 8.dp,
            feedbackClearance = 48.dp,
            isLandscape = true,
            compactHeight = true
        )
    }
}

/** Falls back to [AppDimens.Default] so anything rendered outside [ProvideAppDimens]
 * (an isolated @Preview, a test) degrades gracefully instead of crashing. */
val LocalAppDimens = staticCompositionLocalOf { AppDimens.Default }

/**
 * Measures the available space and provides the matching [AppDimens] down to [content]
 * via [LocalAppDimens]. Wrap a screen's root in this instead of computing the
 * landscape/compact-height breakpoints manually at each call site.
 */
@Composable
fun ProvideAppDimens(
    modifier: Modifier = Modifier.fillMaxSize(),
    content: @Composable () -> Unit
) {
    BoxWithConstraints(modifier = modifier) {
        val isLandscape = maxWidth > maxHeight
        val compactHeight = maxHeight < 480.dp
        val dimens = if (isLandscape && compactHeight) {
            AppDimens.Landscape
        } else {
            AppDimens.Default.copy(isLandscape = isLandscape, compactHeight = compactHeight)
        }
        CompositionLocalProvider(LocalAppDimens provides dimens, content = content)
    }
}
