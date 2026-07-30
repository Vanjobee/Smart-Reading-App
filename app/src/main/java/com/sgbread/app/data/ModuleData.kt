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
            ActivityInfo("m1a1", "Letter Trace", "Trace each letter carefully. Stay on the line and say the letter name aloud.", "trace_letter"),
            ActivityInfo("m1a2", "Letter Hunt", "Look at the letter in the basket, then find the same letter below and put it in the basket.", "letter_basket"),
            ActivityInfo("m1a3", "Letter Match", "Find the same letter and draw a line to connect them.", "match_case")
        )
    )

    val module2 = ModuleInfo(
        id = "module2",
        number = 2,
        title = "Phonics",
        icon = FarmIconKey.SPROUT_SPROUT,
        activities = listOf(
            ActivityInfo("m2a1", "Phonics Sounds", "Click the letter, listen to the sound, then choose the matching picture.", "listen_match"),
            ActivityInfo("m2a2", "Phonics Match", "Click the picture and listen. Then find the first letter.", "tap_letter"),
            ActivityInfo("m2a3", "Phonics Hunt", "Click the 🔊 speaker icon, listen to the sound, then click the matching letter.", "letter_hunt")
        )
    )

    val module3 = ModuleInfo(
        id = "module3",
        number = 3,
        title = "Blending",
        icon = FarmIconKey.SPROUT_GROWING,
        activities = listOf(
            ActivityInfo("m3a2", "Fill in the Letter", "Click the picture and listen to the word. Find the missing letter to complete it.", "missing_letter"),
            ActivityInfo("m3a3", "Blend and Read", "Click each letter sound, blend and read the word, then choose the correct picture.", "blend_read"),
            ActivityInfo("m3a4", "Blending Match", "Click the picture and listen to the word. Match it to the correct word below.", "blending_match")
        )
    )

    val module4 = ModuleInfo(
        id = "module4",
        number = 4,
        title = "Consonant & Vowel Digraphs",
        icon = FarmIconKey.SPROUT_FLOWERING,
        activities = listOf(
            ActivityInfo("m4a1", "Digraph Sound", "Click the picture and listen to the word. Choose the correct digraph sound.", "digraph_build"),
            ActivityInfo("m4a2", "Digraph Match", "Click the picture and listen to the digraph sound. Match it to the correct word.", "picture_word_match"),
            ActivityInfo("m4a3", "Digraph Hunt", "Click and listen to the word with a digraph sound, then choose the matching picture.", "digraph_hunt")
        )
    )

    val all = listOf(module1, module2, module3, module4)

    fun totalActivityCount(): Int = all.sumOf { it.activities.size }
}
