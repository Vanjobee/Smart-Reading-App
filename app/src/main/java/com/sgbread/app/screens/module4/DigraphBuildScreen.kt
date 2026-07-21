package com.sgbread.app.screens.module4

import androidx.compose.runtime.Composable
import com.sgbread.app.audio.AudioManager
import com.sgbread.app.data.LettersBank
import com.sgbread.app.screens.shared.BuildableWord
import com.sgbread.app.screens.shared.WordBuilderScreen

@Composable
fun DigraphBuildScreen(audio: AudioManager, onComplete: () -> Unit, onBack: () -> Unit) {
    WordBuilderScreen(
        title = "Listen and Build",
        instructionVerb = "Build the word",
        words = LettersBank.patternWords.map { BuildableWord(it.word, it.icon) },
        audio = audio,
        onComplete = onComplete,
        onBack = onBack
    )
}
