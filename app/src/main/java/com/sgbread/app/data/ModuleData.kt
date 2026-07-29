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
            ActivityInfo("m1a1", "Letter Trace", "Trace each letter carefully. Stay on the line as you trace. Say the letter's name aloud as you trace.", "trace_letter"),
            ActivityInfo("m1a2", "Letter Hunt", "Look at the letter in the basket. Find the same letter below. Put it in the basket.", "letter_basket"),
            ActivityInfo("m1a3", "Letter Match", "Find the same letter. Draw a line to connect them.", "match_case")
        )
    )

    val module2 = ModuleInfo(
        id = "module2",
        number = 2,
        title = "Phonics",
        icon = FarmIconKey.SPROUT_SPROUT,
        activities = listOf(
            ActivityInfo("m2a1", "Phonics Sounds", "Click the letter and listen to the sound. Choose and click the matching picture.", "listen_match"),
            ActivityInfo("m2a2", "Phonics Match", "Click the picture and listen, then find the first letter.", "tap_letter"),
            ActivityInfo("m2a3", "Phonics Hunt", "Click the 🔊 speaker icon in the upper right corner of your gadget. Listen to the sound. Click the matching letter.", "letter_hunt")
        )
    )

    val module3 = ModuleInfo(
        id = "module3",
        number = 3,
        title = "Blending",
        icon = FarmIconKey.SPROUT_GROWING,
        activities = listOf(
            ActivityInfo("m3a2", "Fill in the Letter", "Click the picture. Listen to the word. Find the missing letter to complete the word.", "missing_letter"),
            ActivityInfo("m3a3", "Blend and Read", "Click each letter. Say the sounds. Blend and read the word. Click the correct picture.", "blend_read"),
            ActivityInfo("m3a4", "Blending Match", "Click the picture. Listen to the word. Match the word below.", "blending_match")
        )
    )

    val module4 = ModuleInfo(
        id = "module4",
        number = 4,
        title = "Consonant & Vowel Digraphs",
        icon = FarmIconKey.SPROUT_FLOWERING,
        activities = listOf(
            ActivityInfo("m4a1", "Digraph Sound", "Click the picture. Listen to the word. Click the correct digraph sound.", "digraph_build"),
            ActivityInfo("m4a2", "Digraph Match", "Click the picture. Listen to the digraph sound. Match the correct word.", "picture_word_match"),
            ActivityInfo("m4a3", "Digraph Hunt", "Click and listen to the word with a digraph sound. Match the digraph sound. Click the picture.", "digraph_hunt")
        )
    )

    val all = listOf(module1, module2, module3, module4)

    fun totalActivityCount(): Int = all.sumOf { it.activities.size }
}
