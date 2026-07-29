package com.sgbread.app.audio

import com.sgbread.app.R

enum class ActivityInstruction(val audioRes: Int) {
    LETTER_TRACE(R.raw.letter_trace_instruction),
    LETTER_HUNT(R.raw.letter_hunt_instruction),
    LETTER_MATCH(R.raw.letter_match_instruction),
    PHONICS_SOUNDS(R.raw.phonics_sounds_instruction),
    PHONICS_MATCH(R.raw.phonics_match_instruction),
    PHONICS_HUNT(R.raw.phonics_hunt_instruction),
    FILL_IN_THE_LETTER(R.raw.fill_in_the_letter_instruction),
    BLEND_AND_READ(R.raw.blend_and_read_instruction),
    BLENDING_MATCH(R.raw.blending_match_instruction),
    DIGRAPH_MATCH(R.raw.digraph_match_instruction),
    DIGRAPH_SOUND(R.raw.digraph_sound_instruction),
    DIGRAPH_HUNT(R.raw.digraph_hunt_instruction)
}

fun AudioManager?.playActivityInstruction(
    instruction: ActivityInstruction,
    onComplete: (() -> Unit)? = null
) {
    this?.playRawResource(
        resourceId = instruction.audioRes,
        key = "activity-instruction:${instruction.name}",
        onComplete = onComplete
    )
}
