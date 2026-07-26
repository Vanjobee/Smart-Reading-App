package com.sgbread.app.data

import com.sgbread.app.R

val ALPHABET: List<Char> = ('A'..'Z').toList()

/** One letter paired with a picture word that starts with its sound, for the Phonics module. */
data class PhonicsItem(val letter: Char, val word: String, val image: Int)

/** A three-letter word used for the blending module, with its picture and recorded word audio.
 * [image] is a real photo (preferred when present); [icon] is the vector fallback. */
data class BlendWord(val word: String, val icon: FarmIconKey? = null, val image: Int? = null, val audio: Int? = null)

/** A word built around a target digraph or blend, used in the Module 4 games.
 * [image] is a real photo (preferred when present); [icon] is the vector fallback. */
data class PatternWord(val word: String, val pattern: String, val icon: FarmIconKey? = null, val image: Int? = null)

object LettersBank {

    /** One picture word per letter, A-Z, used by the Phonics activities. */
    val phonicsItems: List<PhonicsItem> = listOf(
        PhonicsItem('A', "ant", R.drawable.ant),
        PhonicsItem('B', "banana", R.drawable.banana),
        PhonicsItem('C', "cow", R.drawable.cow),
        PhonicsItem('D', "duck", R.drawable.duck),
        PhonicsItem('E', "egg", R.drawable.egg),
        PhonicsItem('F', "frog", R.drawable.frog),
        PhonicsItem('G', "goat", R.drawable.goat),
        PhonicsItem('H', "hen", R.drawable.hen),
        PhonicsItem('I', "insect", R.drawable.insect),
        PhonicsItem('J', "juice", R.drawable.juice),
        PhonicsItem('K', "kite", R.drawable.kite),
        PhonicsItem('L', "ladder", R.drawable.ladder),
        PhonicsItem('M', "mango", R.drawable.mango),
        PhonicsItem('N', "nest", R.drawable.nest),
        PhonicsItem('O', "onion", R.drawable.onion),
        PhonicsItem('P', "pig", R.drawable.pig),
        PhonicsItem('Q', "quail", R.drawable.quail),
        PhonicsItem('R', "rice", R.drawable.rice),
        PhonicsItem('S', "sheep", R.drawable.sheep),
        PhonicsItem('T', "tree", R.drawable.tree),
        PhonicsItem('U', "ube", R.drawable.ube),
        PhonicsItem('V', "vegetables", R.drawable.vegetable),
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
        PatternWord("chick", "CH", FarmIconKey.CHICK, image = R.drawable.chick),
        PatternWord("chicken", "CH", FarmIconKey.CHICKEN),
        PatternWord("sheep", "SH", FarmIconKey.SHEEP, image = R.drawable.sheep),
        PatternWord("shed", "SH", FarmIconKey.SHED),
        PatternWord("shovel", "SH", FarmIconKey.SHOVEL, image = R.drawable.shovel),
        PatternWord("duck", "CK", FarmIconKey.DUCK, image = R.drawable.duck),
        PatternWord("bee", "EE", FarmIconKey.BEE),
        PatternWord("seed", "EE", FarmIconKey.SEED),
        PatternWord("seeds", "EE", FarmIconKey.SEEDS),
        PatternWord("bean", "EA", FarmIconKey.BEAN),
        PatternWord("rain", "AI", FarmIconKey.RAIN),
        PatternWord("goat", "OA", FarmIconKey.GOAT),
        PatternWord("cow", "OW", FarmIconKey.COW, image = R.drawable.cow),
        PatternWord("grain", "GR", FarmIconKey.GRAIN),
        PatternWord("straw", "ST", FarmIconKey.STRAW),
        PatternWord("spade", "SP", FarmIconKey.SPADE),
        PatternWord("fish", "SH", image = R.drawable.fish),
        PatternWord("crab", "CR", image = R.drawable.crab),
        PatternWord("net", "ET", image = R.drawable.net),
        PatternWord("well", "LL", image = R.drawable.well),
        PatternWord("can", "AN", image = R.drawable.can),
        PatternWord("cat", "AT", image = R.drawable.cat),
        PatternWord("hat", "AT", image = R.drawable.hat),
        PatternWord("corn", "OR", image = R.drawable.corn),
        PatternWord("dog", "OG", image = R.drawable.dog),
        PatternWord("log", "OG", image = R.drawable.log),
        PatternWord("flower", "FL", image = R.drawable.flower),
        PatternWord("fruit", "FR", image = R.drawable.fruit),
        PatternWord("glove", "GL", image = R.drawable.glove),
        PatternWord("grass", "GR", image = R.drawable.grass),
        PatternWord("husk", "SK", image = R.drawable.husk),
        PatternWord("hut", "UT", image = R.drawable.hut),
        PatternWord("nut", "UT", image = R.drawable.nut),
        PatternWord("milk", "LK", image = R.drawable.milk),
        PatternWord("moth", "TH", image = R.drawable.moth),
        PatternWord("mud", "UD", image = R.drawable.mud),
        PatternWord("nest", "ST", image = R.drawable.nest_pattern),
        PatternWord("pot", "OT", image = R.drawable.pot),
        PatternWord("sun", "UN", image = R.drawable.sun),
        PatternWord("wheelbarrow", "WH", image = R.drawable.wheelbarrow),
        PatternWord("wind", "ND", image = R.drawable.wind)
    )
}
