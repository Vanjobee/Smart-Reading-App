package com.sgbread.app.audio

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AudioManagerTest {
    @Test
    fun `matches simple letter and word pronunciations with normalized comparison`() {
        assertTrue(evaluateSpeechMatch("a", "ay"))
        assertTrue(evaluateSpeechMatch("ant", "ant"))
        assertTrue(evaluateSpeechMatch("ant", "aunt"))
        assertFalse(evaluateSpeechMatch("ant", "banana"))
    }
}
