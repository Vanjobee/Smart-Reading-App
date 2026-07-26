package com.sgbread.app.ui.components

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sgbread.app.audio.AudioManager
import com.sgbread.app.audio.Sfx
import com.sgbread.app.ui.theme.CreamWhite
import com.sgbread.app.ui.theme.TextBrown

/**
 * Common shell for every activity screen: farm background, a title bar with
 * back navigation and a "hear it again" speaker button, and a bottom-aligned
 * spot for the reinforcement banner.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScaffold(
    title: String,
    onBack: () -> Unit,
    onReplayInstructions: (() -> Unit)?,
    feedback: AnswerFeedback = AnswerFeedback.None,
    audio: AudioManager? = null,
    playFeedbackAudio: Boolean = true,
    blockInputDuringAudio: Boolean = true,
    titleTextScale: Float = 1f,
    content: @Composable (PaddingValues) -> Unit
) {
    val isAudioPlaying by audio?.isPlaying?.collectAsStateWithLifecycle()
        ?: androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val view = LocalView.current

    DisposableEffect(view) {
        val statusBarController = if (!view.isInEditMode) {
            (view.context as? Activity)?.window?.let { window ->
                WindowCompat.getInsetsController(window, view)
            }
        } else {
            null
        }
        val previousLightStatusBars = statusBarController?.isAppearanceLightStatusBars
        statusBarController?.isAppearanceLightStatusBars = true

        onDispose {
            if (previousLightStatusBars != null) {
                statusBarController.isAppearanceLightStatusBars = previousLightStatusBars
            }
        }
    }

    DisposableEffect(audio) {
        onDispose { audio?.stopPlayback() }
    }

    // FarmBackground fills the true screen edge-to-edge; CenterAlignedTopAppBar already
    // insets its own content from the status bar/cutout by default (TopAppBarDefaults.windowInsets),
    // so padding the whole Box here would just double up and leave a gap above the background.
    Box(modifier = Modifier.fillMaxSize()) {
        FarmBackground()
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        val titleStyle = MaterialTheme.typography.titleLarge
                        AutoSizeText(
                            title,
                            style = titleStyle.copy(
                                color = TextBrown,
                                fontSize = titleStyle.fontSize * titleTextScale.coerceIn(1f, 1.25f)
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                if (audio == null) {
                                    onBack()
                                } else {
                                    audio.stopPlayback()
                                    audio.playSfx(Sfx.TAP, onComplete = onBack)
                                }
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextBrown)
                        }
                    },
                    actions = {
                        if (onReplayInstructions != null) {
                            IconButton(
                                onClick = {
                                    audio?.stopPlayback()
                                    onReplayInstructions()
                                }
                            ) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Hear instructions", tint = TextBrown)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = CreamWhite.copy(alpha = 0.78f)
                    )
                )
            }
        ) { padding ->
            BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(padding)) {
                val metrics = activityLayoutMetrics(maxWidth, maxHeight)
                val navPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

                content(PaddingValues(bottom = metrics.feedbackClearance + navPadding))

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = if (metrics.compactHeight) 12.dp else 24.dp + navPadding),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Bottom,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ReinforcementBanner(
                        feedback = feedback,
                        audio = audio,
                        playAudio = playFeedbackAudio
                    )
                }

                if (isAudioPlaying && blockInputDuringAudio) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .semantics { disabled() }
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        awaitPointerEvent().changes.forEach { it.consume() }
                                    }
                                }
                            }
                    )
                }
            }
        }
    }
}
