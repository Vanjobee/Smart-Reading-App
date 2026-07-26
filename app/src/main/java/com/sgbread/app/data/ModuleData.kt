package com.sgbread.app.data

data class ActivityInfo(
    val id: String,
    val title: String,
    val description: String,
    val route: String
)

data class ModuleInfo(
    val id: String,
    val number: Int,
    val title: String,
    val icon: FarmIconKey,
    val activities: List<ActivityInfo>
)

object Modules {

    val module1 = ModuleInfo(
        id = "module1",
        number = 1,
        title = "Letter Recognition",
        icon = FarmIconKey.SPROUT_SEED,
        activities = listOf(
            ActivityInfo("m1a1", "Trace the Letter", "Trace uppercase and lowercase letter pairs", "trace_letter"),
            ActivityInfo("m1a2", "Letter Basket", "Drag letters into the matching basket", "letter_basket"),
            ActivityInfo("m1a3", "Match Upper & Lowercase", "Match letter pairs and meet a farm word", "match_case")
        )
    )

    val module2 = ModuleInfo(
        id = "module2",
        number = 2,
        title = "Phonics",
        icon = FarmIconKey.SPROUT_SPROUT,
        activities = listOf(
            ActivityInfo("m2a1", "Phonics Match", "Listen to the phonics sound and choose its picture", "listen_match"),
            ActivityInfo("m2a2", "Tap the Letter", "See a picture, tap its beginning sound", "tap_letter"),
            ActivityInfo("m2a3", "Phonics Hunt", "Find uppercase and lowercase letters by sound", "letter_hunt")
        )
    )

    val module3 = ModuleInfo(
        id = "module3",
        number = 3,
        title = "Blending (CVC Words)",
        icon = FarmIconKey.SPROUT_GROWING,
        activities = listOf(
            ActivityInfo("m3a2", "Supply the Missing Letter", "Complete the missing letter", "missing_letter"),
            ActivityInfo("m3a3", "Blend and Read", "Listen, blend the sounds, choose the word", "blend_read"),
            ActivityInfo("m3a4", "Blending Match", "Match the picture to the blended word", "blending_match")
        )
    )

    val module4 = ModuleInfo(
        id = "module4",
        number = 4,
        title = "Consonant & Vowel Digraphs",
        icon = FarmIconKey.SPROUT_FLOWERING,
        activities = listOf(
            ActivityInfo("m4a1", "Digraph Sound", "Choose the digraph sound that matches the picture", "digraph_build"),
            ActivityInfo("m4a2", "Picture-to-Word Match", "Tap the picture, choose the spelling", "picture_word_match"),
            ActivityInfo("m4a3", "Digraph Hunt", "Find words with the target digraph", "digraph_hunt")
        )
    )

    val all = listOf(module1, module2, module3, module4)

    fun totalActivityCount(): Int = all.sumOf { it.activities.size }
}
