package com.sgbread.app.audio

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.sgbread.app.R
import java.util.ArrayDeque
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal fun evaluateSpeechMatch(targetText: String, transcript: String): Boolean {
    val target = normalizeSpeechText(targetText)
    val spoken = normalizeSpeechText(transcript)
    if (target.isEmpty() || spoken.isEmpty()) return false
    if (target == spoken) return true

    val targetTokens = target.split(Regex("\\s+")).filter { it.isNotEmpty() }
    val spokenTokens = spoken.split(Regex("\\s+")).filter { it.isNotEmpty() }
    if (targetTokens.isEmpty() || spokenTokens.isEmpty()) return false

    if (targetTokens.size == 1 && spokenTokens.size == 1) {
        val targetWord = targetTokens.first()
        val spokenWord = spokenTokens.first()
        if (targetWord == spokenWord) return true

        val targetVariants = pronunciationVariants(targetWord)
        val spokenVariants = pronunciationVariants(spokenWord)
        if (targetVariants.contains(spokenWord) || spokenVariants.contains(targetWord)) return true

        return approximateWordMatch(targetWord, spokenWord)
    }

    return targetTokens.size == spokenTokens.size &&
        targetTokens.zip(spokenTokens).all { (targetToken, spokenToken) ->
            targetToken == spokenToken || pronunciationVariants(targetToken).contains(spokenToken)
        }
}

private fun normalizeSpeechText(text: String): String {
    return text.lowercase(Locale.US)
        .replace(Regex("[^a-z0-9]+"), " ")
        .trim()
}

private fun pronunciationVariants(token: String): Set<String> {
    return when (token) {
        "a", "ah", "ay", "ei" -> setOf("a", "ah", "ay", "ei")
        "b", "bee", "be" -> setOf("b", "bee", "be")
        "c", "see", "sea", "si" -> setOf("c", "see", "sea", "si")
        "d", "dee", "de" -> setOf("d", "dee", "de")
        "e", "ee", "eh" -> setOf("e", "ee", "eh")
        "f", "eff" -> setOf("f", "eff")
        "g", "gee", "ji" -> setOf("g", "gee", "ji")
        "h", "aitch" -> setOf("h", "aitch")
        "i", "eye", "ih" -> setOf("i", "eye", "ih")
        "j", "jay", "jae" -> setOf("j", "jay", "jae")
        "k", "kay", "ka" -> setOf("k", "kay", "ka")
        "l", "el" -> setOf("l", "el")
        "m", "em" -> setOf("m", "em")
        "n", "en" -> setOf("n", "en")
        "o", "oh", "aw" -> setOf("o", "oh", "aw")
        "p", "pee" -> setOf("p", "pee")
        "q", "cue" -> setOf("q", "cue")
        "r", "ar" -> setOf("r", "ar")
        "s", "ess" -> setOf("s", "ess")
        "t", "tee" -> setOf("t", "tee")
        "u", "you", "oo" -> setOf("u", "you", "oo")
        "v", "vee" -> setOf("v", "vee")
        "w", "doubleyou" -> setOf("w", "doubleyou")
        "x", "ex" -> setOf("x", "ex")
        "y", "why", "wy" -> setOf("y", "why", "wy")
        "z", "zed", "zee" -> setOf("z", "zed", "zee")
        else -> emptySet()
    }
}

private fun approximateWordMatch(targetWord: String, spokenWord: String): Boolean {
    val maxDistance = 1
    val targetChars = targetWord.toCharArray()
    val spokenChars = spokenWord.toCharArray()
    val targetLength = targetChars.size
    val spokenLength = spokenChars.size

    if (kotlin.math.abs(targetLength - spokenLength) > maxDistance) return false

    var differences = 0
    var targetIndex = 0
    var spokenIndex = 0

    while (targetIndex < targetLength && spokenIndex < spokenLength) {
        if (targetChars[targetIndex] != spokenChars[spokenIndex]) {
            differences++
            if (differences > maxDistance) return false
            if (targetLength > spokenLength) {
                targetIndex++
            } else if (spokenLength > targetLength) {
                spokenIndex++
            } else {
                targetIndex++
                spokenIndex++
            }
        } else {
            targetIndex++
            spokenIndex++
        }
    }

    differences += kotlin.math.abs(targetLength - targetIndex - (spokenLength - spokenIndex))
    return differences <= maxDistance
}

enum class Sfx { CORRECT, INCORRECT, TAP, HARVEST }

/**
 * Serializes voice-over, pronunciation, praise, and sound-effect playback.
 * Bundled recordings are preferred; Android TTS is used only when a matching
 * raw resource is unavailable.
 */
class AudioManager private constructor(context: Context) {

    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())

    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
        .build()

    private val soundResources: Map<Sfx, Int> = mapOf(
        Sfx.CORRECT to R.raw.sfx_correct,
        Sfx.INCORRECT to R.raw.sfx_incorrect,
        Sfx.TAP to R.raw.sfx_tap,
        Sfx.HARVEST to R.raw.sfx_harvest
    )

    /** Recorded native pronunciation for each letter's sound. */
    private val letterSoundIds: Map<Char, Int> = mapOf(
        'A' to R.raw.a, 'B' to R.raw.b, 'C' to R.raw.c, 'D' to R.raw.d,
        'E' to R.raw.e, 'F' to R.raw.f, 'G' to R.raw.g, 'H' to R.raw.h,
        'I' to R.raw.i, 'J' to R.raw.j, 'K' to R.raw.k, 'L' to R.raw.l,
        'M' to R.raw.m, 'N' to R.raw.n, 'O' to R.raw.o, 'P' to R.raw.p,
        'Q' to R.raw.q, 'R' to R.raw.r, 'S' to R.raw.s, 'T' to R.raw.t,
        'U' to R.raw.u, 'V' to R.raw.v, 'W' to R.raw.w, 'X' to R.raw.x,
        'Y' to R.raw.y, 'Z' to R.raw.z
    )

    /** Recorded letter name (for example, "ay" for A), as opposed to its phonetic sound. */
    private val letterNameSoundIds: Map<Char, Int> = mapOf(
        'A' to R.raw.letter_name_a, 'B' to R.raw.letter_name_b,
        'C' to R.raw.letter_name_c, 'D' to R.raw.letter_name_d,
        'E' to R.raw.letter_name_e, 'F' to R.raw.letter_name_f,
        'G' to R.raw.letter_name_g, 'H' to R.raw.letter_name_h,
        'I' to R.raw.letter_name_i, 'J' to R.raw.letter_name_j,
        'K' to R.raw.letter_name_k, 'L' to R.raw.letter_name_l,
        'M' to R.raw.letter_name_m, 'N' to R.raw.letter_name_n,
        'O' to R.raw.letter_name_o, 'P' to R.raw.letter_name_p,
        'Q' to R.raw.letter_name_q, 'R' to R.raw.letter_name_r,
        'S' to R.raw.letter_name_s, 'T' to R.raw.letter_name_t,
        'U' to R.raw.letter_name_u, 'V' to R.raw.letter_name_v,
        'W' to R.raw.letter_name_w, 'X' to R.raw.letter_name_x,
        'Y' to R.raw.letter_name_y, 'Z' to R.raw.letter_name_z
    )

    private sealed interface PlaybackRequest {
        val key: String
        val rate: Float
        val onComplete: (() -> Unit)?

        data class Recorded(
            val resourceId: Int,
            override val key: String,
            override val rate: Float = 1f,
            override val onComplete: (() -> Unit)? = null
        ) : PlaybackRequest

        data class Synthesized(
            val text: String,
            override val key: String,
            override val rate: Float = 1f,
            override val onComplete: (() -> Unit)? = null
        ) : PlaybackRequest
    }

    private val playbackQueue = ArrayDeque<PlaybackRequest>()
    private val lastQueuedAt = mutableMapOf<String, Long>()
    private val playedOnceKeys = mutableSetOf<String>()
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    private var currentPlayer: MediaPlayer? = null
    private var activeRequest: PlaybackRequest? = null
    private var playbackActive = false
    private var ttsReady = false
    private var ttsFailed = false
    private lateinit var textToSpeech: TextToSpeech
    private var utteranceCounter = 0L
    private val recognizer = SpeechRecognizer.createSpeechRecognizer(appContext)

    init {
        textToSpeech = TextToSpeech(appContext) { status ->
            mainHandler.post {
                if (status == TextToSpeech.SUCCESS) {
                    configureTextToSpeech()
                    ttsReady = true
                } else {
                    ttsFailed = true
                }
                playNextQueued()
            }
        }
    }

    private fun configureTextToSpeech() {
        textToSpeech.language = Locale.US
        textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) = Unit
            override fun onDone(utteranceId: String?) = finishCurrentPlayback()

            @Deprecated("Deprecated in Android")
            override fun onError(utteranceId: String?) = finishCurrentPlayback()

            override fun onError(utteranceId: String?, errorCode: Int) = finishCurrentPlayback()
        })
    }

    /** Plays a vocabulary word, falling back to Android TTS only when absent. */
    fun playWord(text: String, rate: Float = 1.0f, onComplete: (() -> Unit)? = null) {
        enqueue(listOf(resolvePrompt(text, rate, onComplete)), debounce = onComplete == null)
    }

    /** Plays an instruction or praise line only when a matching recording exists. */
    fun playRecordedPrompt(text: String, rate: Float = 1.0f, onComplete: (() -> Unit)? = null) {
        val request = resolveRecordedPrompt(text, rate, onComplete)
        if (request != null) {
            enqueue(listOf(request), debounce = onComplete == null)
        } else {
            mainHandler.post { onComplete?.invoke() }
        }
    }

    fun playRawResource(resourceId: Int, key: String, onComplete: (() -> Unit)? = null) {
        val request = PlaybackRequest.Recorded(resourceId, key, onComplete = onComplete)
        enqueue(listOf(request), debounce = onComplete == null)
    }

    /** Stops any previous screen's sound and starts this destination's recording. */
    fun replaceWithRawResource(resourceId: Int, key: String) {
        mainHandler.post {
            stopPlaybackNow()
            enqueueNow(listOf(PlaybackRequest.Recorded(resourceId, key)), debounce = false)
        }
    }

    /** Stops any previous screen's sound and starts this destination's prompt. */
    fun replaceWithRecordedPrompt(text: String, rate: Float = 1f) {
        mainHandler.post {
            stopPlaybackNow()
            val request = resolveRecordedPrompt(text, rate)
            if (request != null) {
                enqueueNow(listOf(request), debounce = false)
            }
        }
    }

    fun playRawResourceOnce(resourceId: Int, key: String) {
        mainHandler.post {
            if (playedOnceKeys.add(key)) {
                enqueue(listOf(PlaybackRequest.Recorded(resourceId, key)), debounce = false)
            }
        }
    }

    fun playSfx(sfx: Sfx) {
        val resourceId = soundResources[sfx] ?: return
        enqueue(listOf(PlaybackRequest.Recorded(resourceId, "sfx:$sfx")))
    }

    /** Plays the recorded native pronunciation of [letter]'s sound. */
    fun playLetterSound(letter: Char) {
        val normalized = letter.uppercaseChar()
        val resourceId = letterSoundIds[normalized]
        if (resourceId != null) {
            enqueue(listOf(PlaybackRequest.Recorded(resourceId, "letter-sound:$normalized")))
        } else {
            playWord(normalized.toString())
        }
    }

    /** Plays the recorded name of [letter] (e.g. "ay" for A), distinct from
     * [playLetterSound]'s phonetic sound. */
    fun playLetterName(letter: Char) {
        val normalized = letter.uppercaseChar()
        val resourceId = letterNameSoundIds[normalized]
        if (resourceId != null) {
            enqueue(listOf(PlaybackRequest.Recorded(resourceId, "letter-name:$normalized")))
        } else {
            playWord(normalized.toString())
        }
    }

    /** Plays the recorded letter sound, then the word without overlap. */
    fun speakLetterThenWord(letter: Char, word: String, rate: Float = 1.0f, onComplete: (() -> Unit)? = null) {
        val normalized = letter.uppercaseChar()
        val first = letterSoundIds[normalized]?.let {
            PlaybackRequest.Recorded(it, "letter-sound:$normalized", rate)
        } ?: resolvePrompt(normalized.toString(), rate)
        enqueue(listOf(first, resolvePrompt(word, rate, onComplete)), debounce = false)
    }

    /** Plays the recorded letter *name* (e.g. "ay" for A), then the recorded [word]
     * shortly after -- for completion/reward moments that should teach the letter's
     * name rather than its phonetic sound. */
    fun speakLetterNameThenWord(letter: Char, word: String, rate: Float = 1.0f, onComplete: (() -> Unit)? = null) {
        val normalized = letter.uppercaseChar()
        val first = letterNameSoundIds[normalized]?.let {
            PlaybackRequest.Recorded(it, "letter-name:$normalized", rate)
        } ?: resolvePrompt(normalized.toString(), rate)
        enqueue(listOf(first, resolvePrompt(word, rate, onComplete)), debounce = false)
    }

    /** Plays [intro] then the recorded sound for [letter] without overlap. */
    fun speakThenLetterSound(intro: String, letter: Char, rate: Float = 1.0f) {
        val normalized = letter.uppercaseChar()
        val second = letterSoundIds[normalized]?.let {
            PlaybackRequest.Recorded(it, "letter-sound:$normalized", rate)
        } ?: resolvePrompt(normalized.toString(), rate)
        val requests = listOfNotNull(resolveRecordedPrompt(intro, rate), second)
        enqueue(requests, debounce = false)
    }

    /** Plays recorded letter sounds followed by the vocabulary word. */
    fun playLettersThenWord(word: String, rate: Float = 1f) {
        val requests = word.map { letter ->
            val normalized = letter.uppercaseChar()
            letterSoundIds[normalized]?.let {
                PlaybackRequest.Recorded(it, "letter-sound:$normalized", rate)
            } ?: resolvePrompt(normalized.toString(), rate)
        } + resolvePrompt(word, rate)
        enqueue(requests, debounce = false)
    }

    /** Plays a word with fallback, then a recorded praise line without TTS. */
    fun playWordThenRecorded(
        word: String,
        recordedPrompt: String,
        rate: Float = 1f,
        onComplete: (() -> Unit)? = null
    ) {
        val praise = resolveRecordedPrompt(recordedPrompt, onComplete = onComplete)
        val requests = listOfNotNull(resolvePrompt(word, rate), praise)
        enqueue(requests, debounce = false, clearPending = true)
    }

    /** Plays a digraph/blend's sound by playing each letter's recorded sound. */
    fun playPatternSound(pattern: String) {
        val requests = pattern.map { letter ->
            val normalized = letter.uppercaseChar()
            letterSoundIds[normalized]?.let {
                PlaybackRequest.Recorded(it, "letter-sound:$normalized")
            } ?: resolvePrompt(normalized.toString())
        }
        enqueue(requests, debounce = false)
    }

    private fun resolvePrompt(
        text: String,
        rate: Float = 1f,
        onComplete: (() -> Unit)? = null
    ): PlaybackRequest {
        val resourceId = recordedResourceId(text)
        return if (resourceId != 0) {
            PlaybackRequest.Recorded(resourceId, "raw:${resourceNameFor(text)}", rate, onComplete)
        } else {
            PlaybackRequest.Synthesized(text, "tts:${normalizeSpeechText(text)}", rate, onComplete)
        }
    }

    private fun resolveRecordedPrompt(
        text: String,
        rate: Float = 1f,
        onComplete: (() -> Unit)? = null
    ): PlaybackRequest.Recorded? {
        val resourceId = recordedResourceId(text)
        return if (resourceId == 0) null else {
            PlaybackRequest.Recorded(resourceId, "raw:${resourceNameFor(text)}", rate, onComplete)
        }
    }

    private fun recordedResourceId(text: String): Int {
        val resourceName = resourceNameFor(text)
        return if (resourceName.isEmpty()) 0 else {
            appContext.resources.getIdentifier(resourceName, "raw", appContext.packageName)
        }
    }

    private fun resourceNameFor(text: String): String {
        var resourceName = text.lowercase(Locale.US)
            .replace(Regex("[^a-z0-9]"), "_")
            .trim('_')
        if (resourceName in RESERVED_RESOURCE_NAMES) resourceName += "_"
        return resourceName
    }

    private fun enqueue(
        requests: List<PlaybackRequest>,
        debounce: Boolean = true,
        clearPending: Boolean = false
    ) {
        if (requests.isEmpty()) return
        mainHandler.post {
            enqueueNow(requests, debounce, clearPending)
        }
    }

    private fun enqueueNow(
        requests: List<PlaybackRequest>,
        debounce: Boolean,
        clearPending: Boolean = false
    ) {
        if (clearPending) playbackQueue.clear()
        val now = SystemClock.elapsedRealtime()
        requests.forEach { request ->
            val lastTime = lastQueuedAt[request.key]
            if (!debounce || lastTime == null || now - lastTime >= DEBOUNCE_MS) {
                playbackQueue.addLast(request)
                lastQueuedAt[request.key] = now
            }
        }
        if (playbackActive || playbackQueue.isNotEmpty()) {
            _isPlaying.value = true
        }
        playNextQueued()
    }

    private fun playNextQueued() {
        if (playbackActive) return
        if (playbackQueue.isEmpty()) {
            _isPlaying.value = false
            return
        }
        val request = playbackQueue.first()
        if (request is PlaybackRequest.Synthesized && !ttsReady) {
            if (!ttsFailed) return
            playbackQueue.removeFirst()
            request.onComplete?.invoke()
            playNextQueued()
            return
        }

        playbackQueue.removeFirst()
        playbackActive = true
        activeRequest = request
        when (request) {
            is PlaybackRequest.Recorded -> startRecorded(request)
            is PlaybackRequest.Synthesized -> startSynthesized(request)
        }
    }

    private fun startRecorded(request: PlaybackRequest.Recorded) {
        val player = MediaPlayer.create(appContext, request.resourceId, audioAttributes, 0)
        if (player == null) {
            finishCurrentPlayback()
            return
        }
        currentPlayer = player
        runCatching {
            player.playbackParams = player.playbackParams.setSpeed(request.rate.coerceIn(0.75f, 1.25f))
        }
        player.setOnCompletionListener {
            it.release()
            currentPlayer = null
            finishCurrentPlayback()
        }
        player.setOnErrorListener { failedPlayer, _, _ ->
            failedPlayer.release()
            currentPlayer = null
            finishCurrentPlayback()
            true
        }
        player.start()
    }

    private fun startSynthesized(request: PlaybackRequest.Synthesized) {
        textToSpeech.setSpeechRate(request.rate.coerceIn(0.75f, 1.25f))
        val utteranceId = "sgbread-${++utteranceCounter}"
        val result = textToSpeech.speak(request.text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        if (result == TextToSpeech.ERROR) finishCurrentPlayback()
    }

    private fun finishCurrentPlayback() {
        mainHandler.post {
            val completed = activeRequest ?: return@post
            activeRequest = null
            playbackActive = false
            completed.onComplete?.invoke()
            mainHandler.postDelayed({
                if (playbackQueue.isEmpty()) {
                    _isPlaying.value = false
                } else {
                    playNextQueued()
                }
            }, PLAYBACK_GAP_MS)
        }
    }

    /** Cancels active and queued playback while keeping the manager ready for reuse. */
    fun stopPlayback() {
        mainHandler.post { stopPlaybackNow() }
    }

    private fun stopPlaybackNow() {
        playbackQueue.clear()
        activeRequest = null
        playbackActive = false
        currentPlayer?.setOnCompletionListener(null)
        currentPlayer?.setOnErrorListener(null)
        runCatching { currentPlayer?.stop() }
        currentPlayer?.release()
        currentPlayer = null
        if (::textToSpeech.isInitialized) {
            textToSpeech.stop()
        }
        recognizer.cancel()
        _isPlaying.value = false
    }

    /**
     * Compares the learner's spoken response with the activity target using
     * lightweight normalization and pronunciation heuristics.
     */
    fun analyzeSpokenInput(targetText: String, transcript: String): Boolean {
        return evaluateSpeechMatch(targetText, transcript)
    }

    /**
     * Starts listening for the learner's voice and matches it against [targetText].
     * Returns true in [onResult] if a match is found, false otherwise.
     */
    fun startListening(targetText: String, onResult: (Boolean) -> Unit) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toString())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Say: $targetText")
        }

        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) { onResult(false) }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val success = matches?.firstOrNull()?.let { evaluateSpeechMatch(targetText, it) } ?: false
                onResult(success)
            }
        })
        recognizer.startListening(intent)
    }

    fun shutdown() {
        stopPlaybackNow()
        textToSpeech.stop()
        textToSpeech.shutdown()
        recognizer.destroy()
        mainHandler.removeCallbacksAndMessages(null)
    }

    companion object {
        @Volatile private var instance: AudioManager? = null

        /** Words whose slug collides with a Java reserved keyword and can't be used
         * as an Android resource name; their .mp3 is bundled with a trailing underscore. */
        private val RESERVED_RESOURCE_NAMES = setOf("this")
        private const val PLAYBACK_GAP_MS = 180L
        private const val DEBOUNCE_MS = 350L

        fun getInstance(context: Context): AudioManager =
            instance ?: synchronized(this) {
                instance ?: AudioManager(context).also { instance = it }
            }
    }
}
