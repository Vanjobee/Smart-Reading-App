package com.sgbread.app.data

/**
 * Staged content for a future Module 5 (long vowel teams & consonant digraphs/blends).
 * No screen consumes this yet -- data only, sourced from a recording batch. Reuses
 * [VocabWord]/[SoundFamily]/[VocabLevel] from Vocabulary.kt so it can slot into
 * [Vocabulary.allLevels] later without a type change. Audio for each word resolves
 * dynamically by name via AudioManager.playWord, same as every other word list.
 */
object Module5Data {

    val vowelTeams = VocabLevel(
        number = 8,
        title = "Long Vowel Teams",
        families = listOf(
            SoundFamily("AI", listOf(VocabWord("bait"))),
            SoundFamily("EE", listOf(VocabWord("bee", FarmIconKey.BEE))),
            SoundFamily("OA", listOf(VocabWord("coat"))),
            SoundFamily("OO", listOf(VocabWord("boot"), VocabWord("looks"))),
            SoundFamily("OW", listOf(VocabWord("blow"), VocabWord("fowl"))),
            SoundFamily("IGH", listOf(VocabWord("night"))),
            SoundFamily("OI", listOf(VocabWord("point")))
        )
    )

    val consonantDigraphs = VocabLevel(
        number = 9,
        title = "Consonant Digraphs & Blends",
        families = listOf(
            SoundFamily("SH", listOf(VocabWord("ship"))),
            SoundFamily("TH", listOf(VocabWord("this"), VocabWord("thud"))),
            SoundFamily("CH", listOf(VocabWord("much"))),
            SoundFamily("NG", listOf(VocabWord("lung"))),
            SoundFamily("NK", listOf(VocabWord("rink")))
        )
    )

    val all = listOf(vowelTeams, consonantDigraphs)
}
