package com.sgbread.app.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.speech.tts.TextToSpeech
import com.sgbread.app.R
import java.util.Locale

enum class Sfx { CORRECT, INCORRECT, TAP, HARVEST }

/**
 * Wraps Android's TextToSpeech for letter/word/instruction voice-over and a
 * small SoundPool for short feedback chimes. TTS speech rate is slowed
 * slightly and pitch kept neutral for clear, standard pronunciation.
 */
class AudioManager private constructor(context: Context) {

    private val appContext = context.applicationContext
    private var ttsReady = false
    private val pendingUtterances = ArrayDeque<Pair<String, Float>>()

    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(appContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.setLanguage(Locale.US)
                tts?.setPitch(1.0f)
                ttsReady = true
                while (pendingUtterances.isNotEmpty()) {
                    val (text, rate) = pendingUtterances.removeFirst()
                    speakInternal(text, rate)
                }
            }
        }
    }

    private val soundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val soundIds: Map<Sfx, Int> = mapOf(
        Sfx.CORRECT to soundPool.load(appContext, R.raw.sfx_correct, 1),
        Sfx.INCORRECT to soundPool.load(appContext, R.raw.sfx_incorrect, 1),
        Sfx.TAP to soundPool.load(appContext, R.raw.sfx_tap, 1),
        Sfx.HARVEST to soundPool.load(appContext, R.raw.sfx_harvest, 1)
    )

    /** Speaks a letter name, word, or instruction aloud. [rate] < 1 is slower, useful for single letters. */
    fun speak(text: String, rate: Float = 0.9f) {
        if (ttsReady) speakInternal(text, rate) else pendingUtterances.addLast(text to rate)
    }

    private fun speakInternal(text: String, rate: Float) {
        tts?.setSpeechRate(rate)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, Bundle(), "sgb_utterance_${text.hashCode()}")
    }

    fun playSfx(sfx: Sfx) {
        val id = soundIds[sfx] ?: return
        soundPool.play(id, 1f, 1f, 1, 0, 1f)
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        soundPool.release()
    }

    companion object {
        @Volatile private var instance: AudioManager? = null

        fun getInstance(context: Context): AudioManager =
            instance ?: synchronized(this) {
                instance ?: AudioManager(context).also { instance = it }
            }
    }
}
