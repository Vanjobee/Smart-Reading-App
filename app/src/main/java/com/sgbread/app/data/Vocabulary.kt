package com.sgbread.app.data

/** A single vocabulary word from the SGB-READ word list. */
data class VocabWord(
    val text: String,
    val icon: FarmIconKey? = null
)

/** A digraph or blend family with its example words, per the SGB-READ progression. */
data class SoundFamily(
    val pattern: String,
    val words: List<VocabWord>
)

data class VocabLevel(
    val number: Int,
    val title: String,
    val words: List<VocabWord> = emptyList(),
    val families: List<SoundFamily> = emptyList()
)

/**
 * The full SGB-READ vocabulary progression, Level 1 (three-letter words)
 * through Level 7 (advanced farm vocabulary).
 */
object Vocabulary {

    val level1 = VocabLevel(
        number = 1,
        title = "CVC (Consonant–Vowel–Consonant)",
        words = listOf(
            VocabWord("cat", FarmIconKey.CAT),
            VocabWord("dog", FarmIconKey.DOG),
            VocabWord("hen", FarmIconKey.HEN),
            VocabWord("hat"),
            VocabWord("mud", FarmIconKey.MUD),
            VocabWord("nut"),
            VocabWord("pig", FarmIconKey.PIG),
            VocabWord("rat", FarmIconKey.RAT),
            VocabWord("sun", FarmIconKey.SUN),
            VocabWord("wet", FarmIconKey.WET)
        )
    )

    val level2 = VocabLevel(
        number = 2,
        title = "CVCC (Consonant–Vowel–Consonant–Consonant)",
        words = listOf(
            VocabWord("barn", FarmIconKey.BARN),
            VocabWord("calf"),
            VocabWord("corn", FarmIconKey.CORN),
            VocabWord("farm"),
            VocabWord("milk", FarmIconKey.MILK),
            VocabWord("wind", FarmIconKey.WIND),
            VocabWord("nest")
        )
    )

    val level3 = VocabLevel(
        number = 3,
        title = "Consonant Digraphs (ch, sh, th, ck)",
        families = listOf(
            SoundFamily("CH", listOf(VocabWord("chick", FarmIconKey.CHICK), VocabWord("chicken", FarmIconKey.CHICKEN), VocabWord("chop"))),
            SoundFamily("SH", listOf(VocabWord("sheep", FarmIconKey.SHEEP), VocabWord("shed", FarmIconKey.SHED), VocabWord("shovel", FarmIconKey.SHOVEL))),
            SoundFamily("TH", listOf(VocabWord("thresh"))),
            SoundFamily("CK", listOf(VocabWord("duck", FarmIconKey.DUCK), VocabWord("sack", FarmIconKey.SACK)))
        )
    )

    val level4 = VocabLevel(
        number = 4,
        title = "Vowel Digraphs (ee, ea, ai, oa, ay, ow, oi)",
        families = listOf(
            SoundFamily("EE", listOf(VocabWord("bee", FarmIconKey.BEE), VocabWord("feed"), VocabWord("seed", FarmIconKey.SEED), VocabWord("seeds", FarmIconKey.SEEDS), VocabWord("weed", FarmIconKey.WEED), VocabWord("tree"))),
            SoundFamily("EA", listOf(VocabWord("heat", FarmIconKey.HEAT), VocabWord("bean", FarmIconKey.BEAN))),
            SoundFamily("AI", listOf(VocabWord("pail", FarmIconKey.PAIL), VocabWord("rain", FarmIconKey.RAIN), VocabWord("quail"))),
            SoundFamily("UI", listOf(VocabWord("juice"))),
            SoundFamily("OA", listOf(VocabWord("goat", FarmIconKey.GOAT))),
            SoundFamily("AY", listOf(VocabWord("hay"))),
            SoundFamily("OW", listOf(VocabWord("cow", FarmIconKey.COW))),
            SoundFamily("OI", listOf(VocabWord("soil", FarmIconKey.SOIL)))
        )
    )

    val level5 = VocabLevel(
        number = 5,
        title = "Consonant Blends (two consonant sounds, not a digraph)",
        families = listOf(
            SoundFamily("DR", listOf(VocabWord("dry"))),
            SoundFamily("FR", listOf(VocabWord("frog", FarmIconKey.FROG))),
            SoundFamily("KR", listOf(VocabWord("okra", FarmIconKey.OKRA)))
        )
    )

    /** Doesn't fit the CVC/CVCC/digraph/blend patterns above cleanly. Numbered 10 (not
     * 6) to avoid colliding with [level6]/[level7] below and with Module5Data's 8/9. */
    val levelOther = VocabLevel(
        number = 10,
        title = "Other / Irregular",
        words = listOf(
            VocabWord("ant", FarmIconKey.ANT),
            VocabWord("egg", FarmIconKey.EGG),
            VocabWord("ube"),
            VocabWord("rice", FarmIconKey.RICE),
            VocabWord("rope", FarmIconKey.ROPE)
        )
    )

    val level6 = VocabLevel(
        number = 6,
        title = "Longer Farm Vocabulary",
        words = listOf(
            VocabWord("basket", FarmIconKey.BASKET),
            VocabWord("banana", FarmIconKey.BANANA),
            VocabWord("coffee", FarmIconKey.COFFEE),
            VocabWord("garlic", FarmIconKey.GARLIC),
            VocabWord("honey", FarmIconKey.HONEY),
            VocabWord("ladder", FarmIconKey.LADDER),
            VocabWord("lemon", FarmIconKey.LEMON),
            VocabWord("mango", FarmIconKey.MANGO),
            VocabWord("melon", FarmIconKey.MELON),
            VocabWord("onion", FarmIconKey.ONION),
            VocabWord("papaya", FarmIconKey.PAPAYA),
            VocabWord("peanut", FarmIconKey.PEANUT),
            VocabWord("pigeon"),
            VocabWord("rabbit"),
            VocabWord("radish", FarmIconKey.RADISH),
            VocabWord("shovel", FarmIconKey.SHOVEL),
            VocabWord("squash", FarmIconKey.SQUASH),
            VocabWord("tomato", FarmIconKey.TOMATO),
            VocabWord("turkey"),
            VocabWord("water")
        )
    )

    val level7 = VocabLevel(
        number = 7,
        title = "Advanced Farm Vocabulary",
        words = listOf(
            VocabWord("chicken", FarmIconKey.CHICKEN),
            VocabWord("harvest", FarmIconKey.SPROUT_HARVEST),
            VocabWord("manure"),
            VocabWord("piglet", FarmIconKey.PIG)
        )
    )

    val allLevels = listOf(level1, level2, level3, level4, level5, levelOther, level6, level7)
}
