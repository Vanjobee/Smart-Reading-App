package com.sgbread.app.screens.module3

import androidx.compose.runtime.Composable
import com.sgbread.app.audio.AudioManager
import com.sgbread.app.data.LettersBank
import com.sgbread.app.screens.shared.BuildableWord
import com.sgbread.app.screens.shared.WordBuilderScreen

@Composable
fun BuildWordScreen(audio: AudioManager, onComplete: () -> Unit, onBack: () -> Unit) {
    WordBuilderScreen(
        title = "Build the Word",
        instructionVerb = "Build the word",
        words = LettersBank.blendWords.map { BuildableWord(it.word, it.icon) },
        audio = audio,
        onComplete = onComplete,
        onBack = onBack
    )
}
