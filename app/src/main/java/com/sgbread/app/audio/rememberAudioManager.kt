package com.sgbread.app.audio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/** Provides the shared [AudioManager] singleton, created lazily off the current context. */
@Composable
fun rememberAudioManager(): AudioManager {
    val context = LocalContext.current
    return remember { AudioManager.getInstance(context) }
}
