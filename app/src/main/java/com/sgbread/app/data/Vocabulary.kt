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
        title = "Three-Letter Words (CVC & Simple Words)",
        words = listOf(
            VocabWord("ant", FarmIconKey.ANT),
            VocabWord("cat", FarmIconKey.CAT),
            VocabWord("cow", FarmIconKey.COW),
            VocabWord("dog", FarmIconKey.DOG),
            VocabWord("dry"),
            VocabWord("egg", FarmIconKey.EGG),
            VocabWord("hay", FarmIconKey.STRAW),
            VocabWord("hen", FarmIconKey.HEN),
            VocabWord("hat"),
            VocabWord("mud", FarmIconKey.MUD),
            VocabWord("nut"),
            VocabWord("pig", FarmIconKey.PIG),
            VocabWord("rat", FarmIconKey.RAT),
            VocabWord("sun", FarmIconKey.SUN),
            VocabWord("ube"),
            VocabWord("wet", FarmIconKey.WET)
        )
    )

    val level2 = VocabLevel(
        number = 2,
        title = "Four- and Five-Letter Words",
        words = listOf(
            VocabWord("barn", FarmIconKey.BARN),
            VocabWord("calf"),
            VocabWord("corn", FarmIconKey.CORN),
            VocabWord("duck", FarmIconKey.DUCK),
            VocabWord("farm", FarmIconKey.BARN),
            VocabWord("frog", FarmIconKey.FROG),
            VocabWord("goat", FarmIconKey.GOAT),
            VocabWord("heat", FarmIconKey.HEAT),
            VocabWord("milk", FarmIconKey.MILK),
            VocabWord("okra", FarmIconKey.OKRA),
            VocabWord("pail", FarmIconKey.PAIL),
            VocabWord("rain", FarmIconKey.RAIN),
            VocabWord("rice", FarmIconKey.RICE),
            VocabWord("rope", FarmIconKey.ROPE),
            VocabWord("sack", FarmIconKey.SACK),
            VocabWord("soil", FarmIconKey.SOIL),
            VocabWord("weed", FarmIconKey.WEED),
            VocabWord("wind", FarmIconKey.WIND)
        )
    )

    val level3 = VocabLevel(
        number = 3,
        title = "Consonant Digraphs",
        families = listOf(
            SoundFamily("CH", listOf(VocabWord("chick", FarmIconKey.CHICK), VocabWord("chicken", FarmIconKey.CHICKEN), VocabWord("chop"))),
            SoundFamily("SH", listOf(VocabWord("sheep", FarmIconKey.SHEEP), VocabWord("shed", FarmIconKey.SHED), VocabWord("shovel", FarmIconKey.SHOVEL))),
            SoundFamily("TH", listOf(VocabWord("thresh", FarmIconKey.GRAIN))),
            SoundFamily("CK", listOf(VocabWord("duck", FarmIconKey.DUCK)))
        )
    )

    val level4 = VocabLevel(
        number = 4,
        title = "Vowel Digraphs",
        families = listOf(
            SoundFamily("EE", listOf(VocabWord("bee", FarmIconKey.BEE), VocabWord("feed"), VocabWord("seed", FarmIconKey.SEED), VocabWord("seeds", FarmIconKey.SEEDS))),
            SoundFamily("EA", listOf(VocabWord("bean", FarmIconKey.BEAN))),
            SoundFamily("AI", listOf(VocabWord("rain", FarmIconKey.RAIN))),
            SoundFamily("OA", listOf(VocabWord("goat", FarmIconKey.GOAT)))
        )
    )

    val level5 = VocabLevel(
        number = 5,
        title = "Consonant Blends",
        families = listOf(
            SoundFamily("GR", listOf(VocabWord("grain", FarmIconKey.GRAIN), VocabWord("grass"), VocabWord("graft"))),
            SoundFamily("PL", listOf(VocabWord("plant", FarmIconKey.GARDEN_PLANT), VocabWord("plow"), VocabWord("plows"))),
            SoundFamily("PR", listOf(VocabWord("prune"))),
            SoundFamily("SP", listOf(VocabWord("spade", FarmIconKey.SPADE), VocabWord("spray"), VocabWord("sprayer"))),
            SoundFamily("ST", listOf(VocabWord("straw", FarmIconKey.STRAW)))
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

    val allLevels = listOf(level1, level2, level3, level4, level5, level6, level7)
}
