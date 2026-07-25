package com.sgbread.app.screens.module4

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.sgbread.app.audio.AudioManager
import com.sgbread.app.data.LettersBank
import com.sgbread.app.screens.shared.BuildableWord
import com.sgbread.app.screens.shared.WordBuilderScreen
import com.sgbread.app.ui.theme.SgbReadTheme

// True consonant/vowel digraphs only (two letters, one sound) -- excludes plain
// CVC endings and consonant blends that also live in LettersBank.patternWords.
private val DIGRAPH_PATTERNS = setOf("CH", "SH", "TH", "WH", "CK", "EE", "EA", "AI", "OA", "OW")

@Composable
fun DigraphBuildScreen(audio: AudioManager, onComplete: () -> Unit, onBack: () -> Unit) {
    val digraphWords = LettersBank.patternWords.filter { it.pattern in DIGRAPH_PATTERNS }
    WordBuilderScreen(
        title = "Listen and Build",
        instructionVerb = "Build the word",
        words = digraphWords.map { BuildableWord(it.word, it.icon, it.image) },
        audio = audio,
        onComplete = onComplete,
        onBack = onBack
    )
}

@Preview(device = "spec:width=360dp,height=800dp,orientation=portrait", showBackground = true)
@Composable
private fun DigraphBuildScreenPreview() {
    SgbReadTheme {
        DigraphBuildScreen(
            audio = AudioManager.getInstance(LocalContext.current),
            onComplete = {},
            onBack = {}
        )
    }
}
