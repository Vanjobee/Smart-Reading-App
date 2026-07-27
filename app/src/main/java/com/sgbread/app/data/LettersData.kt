package com.sgbread.app.data

import com.sgbread.app.R

val ALPHABET: List<Char> = ('A'..'Z').toList()

/** One letter paired with a picture word that starts with its sound, for the Phonics module. */
data class PhonicsItem(val letter: Char, val word: String, val image: Int)

/** A three-letter word used for the blending module, with its picture and recorded word audio.
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
        PhonicsItem('F', "frog", R.drawable.frog),
        PhonicsItem('G', "goat", R.drawable.goat),
        PhonicsItem('H', "hen", R.drawable.hen),
        PhonicsItem('I', "ink", R.drawable.phonics_ink),
        PhonicsItem('J', "juice", R.drawable.juice),
        PhonicsItem('K', "kite", R.drawable.kite),
        PhonicsItem('L', "log", R.drawable.phonics_log),
        PhonicsItem('M', "mud", R.drawable.phonics_mud),
        PhonicsItem('N', "nest", R.drawable.nest),
        PhonicsItem('O', "owl", R.drawable.phonics_owl),
        PhonicsItem('P', "pig", R.drawable.pig),
        PhonicsItem('Q', "quail", R.drawable.quail),
        PhonicsItem('R', "rice", R.drawable.rice),
        PhonicsItem('S', "sun", R.drawable.phonics_sun),
        PhonicsItem('T', "tree", R.drawable.tree),
        PhonicsItem('U', "up", R.drawable.phonics_up),
        PhonicsItem('V', "van", R.drawable.phonics_van),
        PhonicsItem('W', "watermelon", R.drawable.watermelon),
        PhonicsItem('X', "xylophone", R.drawable.xylophone),
        PhonicsItem('Y', "yoyo", R.drawable.yo_yo),
        PhonicsItem('Z', "zebra", R.drawable.zebra)
    )

    val blendWords: List<BlendWord> = listOf(
        BlendWord("bag", image = R.drawable.blend_bag, audio = R.raw.blend_bag),
        BlendWord("bat", image = R.drawable.blend_bat, audio = R.raw.blend_bat),
        BlendWord("cat", image = R.drawable.blend_cat, audio = R.raw.blend_cat),
        BlendWord("can", image = R.drawable.blend_can, audio = R.raw.blend_can),
        BlendWord("cap", image = R.drawable.blend_cap, audio = R.raw.blend_cap),
        BlendWord("fan", image = R.drawable.blend_fan, audio = R.raw.blend_fan),
        BlendWord("hat", image = R.drawable.blend_hat, audio = R.raw.blend_hat),
        BlendWord("van", image = R.drawable.blend_van, audio = R.raw.blend_van),
        BlendWord("bed", image = R.drawable.blend_bed, audio = R.raw.blend_bed),
        BlendWord("leg", image = R.drawable.blend_leg, audio = R.raw.blend_leg),
        BlendWord("pen", image = R.drawable.blend_pen, audio = R.raw.blend_pen),
        BlendWord("red", image = R.drawable.blend_red, audio = R.raw.blend_red),
        BlendWord("web", image = R.drawable.blend_web, audio = R.raw.blend_web),
        BlendWord("dig", image = R.drawable.blend_dig, audio = R.raw.blend_dig),
        BlendWord("pin", image = R.drawable.blend_pin, audio = R.raw.blend_pin),
        BlendWord("sit", image = R.drawable.blend_sit, audio = R.raw.blend_sit),
        BlendWord("wig", image = R.drawable.blend_wig, audio = R.raw.blend_wig),
        BlendWord("mop", image = R.drawable.blend_mop, audio = R.raw.blend_mop),
        BlendWord("pot", image = R.drawable.blend_pot, audio = R.raw.blend_pot),
        BlendWord("bug", image = R.drawable.blend_bug, audio = R.raw.blend_bug),
        BlendWord("bus", image = R.drawable.blend_bus, audio = R.raw.blend_bus),
        BlendWord("hut", image = R.drawable.blend_hut, audio = R.raw.blend_hut),
        BlendWord("run", image = R.drawable.blend_run, audio = R.raw.blend_run),
        BlendWord("ube", image = R.drawable.blend_ube, audio = R.raw.blend_ube)
    )

    val patternWords: List<PatternWord> = listOf(
        PatternWord("shell", "SH", image = R.drawable.digraph_shell, audio = R.raw.digraph_shell),
        PatternWord("shirt", "SH", image = R.drawable.digraph_shirt, audio = R.raw.digraph_shirt),
        PatternWord("shed", "SH", image = R.drawable.digraph_shed, audio = R.raw.digraph_shed),
        PatternWord("fish", "SH", image = R.drawable.digraph_fish, audio = R.raw.digraph_fish),
        PatternWord("wash", "SH", image = R.drawable.digraph_wash, audio = R.raw.digraph_wash),
        PatternWord("trash", "SH", image = R.drawable.digraph_trash, audio = R.raw.digraph_trash),
        PatternWord("chick", "CH", image = R.drawable.digraph_chick, audio = R.raw.digraph_chick),
        PatternWord("chat", "CH", image = R.drawable.digraph_chat, audio = R.raw.digraph_chat),
        PatternWord("chin", "CH", image = R.drawable.digraph_chin, audio = R.raw.digraph_chin),
        PatternWord("chair", "CH", image = R.drawable.digraph_chair, audio = R.raw.digraph_chair),
        PatternWord("lick", "CK", image = R.drawable.digraph_lick, audio = R.raw.digraph_lick),
        PatternWord("rock", "CK", image = R.drawable.digraph_rock, audio = R.raw.digraph_rock),
        PatternWord("lock", "CK", image = R.drawable.digraph_lock, audio = R.raw.digraph_lock),
        PatternWord("pick", "CK", image = R.drawable.digraph_pick, audio = R.raw.digraph_pick),
        PatternWord("sack", "CK", image = R.drawable.digraph_sack, audio = R.raw.digraph_sack),
        PatternWord("ring", "NG", image = R.drawable.digraph_ring, audio = R.raw.digraph_ring),
        PatternWord("king", "NG", image = R.drawable.digraph_king, audio = R.raw.digraph_king),
        PatternWord("wing", "NG", image = R.drawable.digraph_wing, audio = R.raw.digraph_wing),
        PatternWord("sing", "NG", image = R.drawable.digraph_sing, audio = R.raw.digraph_sing),
        PatternWord("phone", "PH", image = R.drawable.digraph_phone, audio = R.raw.digraph_phone),
        PatternWord("photo", "PH", image = R.drawable.digraph_photo, audio = R.raw.digraph_photo),
        PatternWord("graph", "PH", image = R.drawable.digraph_graph, audio = R.raw.digraph_graph),
        PatternWord("bath", "TH", image = R.drawable.digraph_bath, audio = R.raw.digraph_bath),
        PatternWord("cloth", "TH", image = R.drawable.digraph_cloth, audio = R.raw.digraph_cloth)
    )
}
