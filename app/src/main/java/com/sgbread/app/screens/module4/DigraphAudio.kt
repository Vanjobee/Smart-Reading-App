package com.sgbread.app.screens.module4

import com.sgbread.app.R
import com.sgbread.app.audio.AudioManager
import com.sgbread.app.data.PatternWord

private val DIGRAPH_SOUND_AUDIO = mapOf(
    "CH" to R.raw.digraph_sound_ch,
    "SH" to R.raw.digraph_sound_sh,
    "CK" to R.raw.digraph_sound_ck,
    "NG" to R.raw.digraph_sound_ng,
    "PH" to R.raw.digraph_sound_ph,
    "TH" to R.raw.digraph_sound_th
)

internal fun AudioManager?.playPatternWord(
    word: PatternWord,
    rate: Float = 1f,
    onComplete: (() -> Unit)? = null
) {
    val wordAudio = word.audio
    if (wordAudio != null) {
        this?.playRawResource(wordAudio, "digraph-word:${word.word}", onComplete)
    } else {
        this?.playWord(word.word, rate, onComplete)
    }
}

internal fun AudioManager?.playDigraphSound(pattern: String) {
    val normalized = pattern.uppercase()
    val audio = DIGRAPH_SOUND_AUDIO[normalized]
    if (audio != null) {
        this?.playRawResource(audio, "digraph-sound:$normalized")
    } else {
        this?.playPatternSound(normalized)
    }
}
