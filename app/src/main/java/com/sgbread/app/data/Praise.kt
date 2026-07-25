package com.sgbread.app.data

/**
 * Praise/encouragement phrases. Each string doubles as the lookup key for its
 * pre-recorded voice-over line (see [com.sgbread.app.audio.AudioManager.playRecordedPrompt]),
 * so keep phrasing free of internal punctuation (commas, apostrophes) since
 * those don't collapse the way a single trailing "!" does when sanitized to a
 * resource name — only add a new phrase here once a matching raw audio file exists.
 */
object Praise {
    val correct = listOf(
        "Great Job!", "Excellent!", "Amazing!", "Fantastic!",
        "Nice Work!", "Thats Right!", "You Did It!", "Congratulations!"
    )
    val encouragement = listOf(
        "Try Again!", "Keep Going!", "More Practice!", "Uh Oh lets try another letter!"
    )

    fun randomCorrect(): String = correct.random()
    fun randomEncouragement(): String = encouragement.random()
}
