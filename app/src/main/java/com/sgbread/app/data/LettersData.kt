package com.sgbread.app.data

import com.sgbread.app.R

val ALPHABET: List<Char> = ('A'..'Z').toList()

/** One letter paired with a picture word that starts with its sound, for the Phonics module. */
data class PhonicsItem(val letter: Char, val word: String, val image: Int)

/** A word used for the blending module, with its picture and recorded word audio.
 * [image] is a real photo (preferred when present); [icon] is the vector fallback. */
data class BlendWord(val word: String, val icon: FarmIconKey? = null, val image: Int? = null, val audio: Int? = null)

/** A word built around a target digraph, used in the Module 4 games.
 * [image] is a real photo (preferred when present); [icon] is the vector fallback. */
data class PatternWord(
    val word: String,
    val pattern: String,
    val icon: FarmIconKey? = null,
    val image: Int? = null,
    val audio: Int? = null
)

object LettersBank {

    /** One picture word per letter, A-Z, used by the Phonics activities. */
    val phonicsItems: List<PhonicsItem> = listOf(
        PhonicsItem('A', "ant", R.drawable.ant),
        PhonicsItem('B', "bird", R.drawable.phonics_bird),
        PhonicsItem('C', "cat", R.drawable.phonics_cat),
        PhonicsItem('D', "dig", R.drawable.phonics_dig),
        PhonicsItem('E', "egg", R.drawable.egg),
        PhonicsItem('F', "fish", R.drawable.phonics_fish),
        PhonicsItem('G', "goat", R.drawable.goat),
        PhonicsItem('H', "hen", R.drawable.hen),
        PhonicsItem('I', "ink", R.drawable.phonics_ink),
        PhonicsItem('J', "jam", R.drawable.phonics_jam),
        PhonicsItem('K', "kiwi", R.drawable.phonics_kiwi),
        PhonicsItem('L', "log", R.drawable.phonics_log),
        PhonicsItem('M', "mud", R.drawable.phonics_mud),
        PhonicsItem('N', "nest", R.drawable.nest),
        PhonicsItem('O', "oval", R.drawable.phonics_oval),
        PhonicsItem('P', "pig", R.drawable.pig),
        PhonicsItem('Q', "queen", R.drawable.phonics_queen),
        PhonicsItem('R', "red", R.drawable.phonics_red),
        PhonicsItem('S', "sun", R.drawable.phonics_sun),
        PhonicsItem('T', "tree", R.drawable.tree),
        PhonicsItem('U', "utensils", R.drawable.phonics_utensils),
        PhonicsItem('V', "van", R.drawable.phonics_van),
        PhonicsItem('W', "wheel", R.drawable.phonics_wheel),
        PhonicsItem('X', "xray", R.drawable.phonics_xray),
        PhonicsItem('Y', "yoyo", R.drawable.yo_yo),
        PhonicsItem('Z', "zebra", R.drawable.zebra)
    )

    val blendWords: List<BlendWord> = listOf(
        BlendWord("pigpen", image = R.drawable.blend_pigpen, audio = R.raw.blend_pigpen),
        BlendWord("barn", image = R.drawable.blend_barn, audio = R.raw.blend_barn),
        BlendWord("bee", image = R.drawable.blend_bee, audio = R.raw.blend_bee),
        BlendWord("bird", image = R.drawable.blend_bird, audio = R.raw.blend_bird),
        BlendWord("pig", image = R.drawable.blend_pig, audio = R.raw.blend_pig),
        BlendWord("plant", image = R.drawable.blend_plant, audio = R.raw.blend_plant),
        BlendWord("potato", image = R.drawable.blend_potato, audio = R.raw.blend_potato),
        BlendWord("seed", image = R.drawable.blend_seed, audio = R.raw.blend_seed),
        BlendWord("sugar", image = R.drawable.blend_sugar, audio = R.raw.blend_sugar),
        BlendWord("corn", image = R.drawable.blend_corn, audio = R.raw.blend_corn),
        BlendWord("cat", image = R.drawable.blend_cat, audio = R.raw.blend_cat),
        BlendWord("bat", image = R.drawable.blend_bat, audio = R.raw.blend_bat),
        BlendWord("leg", image = R.drawable.blend_leg, audio = R.raw.blend_leg),
        BlendWord("bus", image = R.drawable.blend_bus, audio = R.raw.blend_bus),
        BlendWord("hut", image = R.drawable.blend_hut, audio = R.raw.blend_hut),
        BlendWord("nut", image = R.drawable.blend_nut, audio = R.raw.blend_nut),
        BlendWord("bed", image = R.drawable.blend_bed, audio = R.raw.blend_bed),
        BlendWord("bug", image = R.drawable.blend_bug, audio = R.raw.blend_bug),
        BlendWord("dig", image = R.drawable.blend_dig, audio = R.raw.blend_dig),
        BlendWord("web", image = R.drawable.blend_web, audio = R.raw.blend_web),
        BlendWord("van", image = R.drawable.blend_van, audio = R.raw.blend_van),
        BlendWord("sit", image = R.drawable.blend_sit, audio = R.raw.blend_sit),
        BlendWord("hat", image = R.drawable.blend_hat, audio = R.raw.blend_hat),
        BlendWord("red", image = R.drawable.blend_red, audio = R.raw.blend_red),
        BlendWord("jam", image = R.drawable.blend_jam, audio = R.raw.blend_jam),
        BlendWord("oval", image = R.drawable.blend_oval, audio = R.raw.blend_oval),
        BlendWord("kiwi", image = R.drawable.blend_kiwi, audio = R.raw.blend_kiwi),
        BlendWord("ant", image = R.drawable.blend_ant, audio = R.raw.blend_ant)
    )

    val patternWords: List<PatternWord> = listOf(
        PatternWord("chicken", "CH", image = R.drawable.digraph_chicken, audio = R.raw.digraph_chicken),
        PatternWord("fish", "SH", image = R.drawable.digraph_fish, audio = R.raw.digraph_fish),
        PatternWord("lunch", "CH", image = R.drawable.digraph_lunch, audio = R.raw.digraph_lunch),
        PatternWord("photo", "PH", image = R.drawable.digraph_photo, audio = R.raw.digraph_photo),
        PatternWord("wheel", "WH", image = R.drawable.digraph_wheel, audio = R.raw.digraph_wheel),
        PatternWord("duck", "CK", image = R.drawable.digraph_duck, audio = R.raw.digraph_duck),
        PatternWord("sing", "NG", image = R.drawable.digraph_sing, audio = R.raw.digraph_sing),
        PatternWord("ring", "NG", image = R.drawable.digraph_ring, audio = R.raw.digraph_ring),
        PatternWord("chain", "CH", image = R.drawable.digraph_chain, audio = R.raw.digraph_chain),
        PatternWord("thumb", "TH", image = R.drawable.digraph_thumb, audio = R.raw.digraph_thumb),
        PatternWord("shell", "SH", image = R.drawable.digraph_shell, audio = R.raw.digraph_shell),
        PatternWord("rock", "CK", image = R.drawable.digraph_rock, audio = R.raw.digraph_rock),
        PatternWord("sack", "CK", image = R.drawable.digraph_sack, audio = R.raw.digraph_sack),
        PatternWord("lick", "CK", image = R.drawable.digraph_lick, audio = R.raw.digraph_lick),
        PatternWord("king", "NG", image = R.drawable.digraph_king, audio = R.raw.digraph_king)
    )
}
