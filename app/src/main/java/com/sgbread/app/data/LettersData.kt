package com.sgbread.app.data

val ALPHABET: List<Char> = ('A'..'Z').toList()

/** One letter paired with a farm word that starts with its sound, for the Phonics module. */
data class PhonicsItem(val letter: Char, val word: String, val icon: FarmIconKey)

/** A three/four-letter word used for the blending (CVC) module, with its picture. */
data class BlendWord(val word: String, val icon: FarmIconKey)

/** A word built around a target digraph or blend, used in the Module 4 games. */
data class PatternWord(val word: String, val pattern: String, val icon: FarmIconKey)

object LettersBank {

    /** Letters that have a clear farm-themed picture, used by the Phonics activities. */
    val phonicsItems: List<PhonicsItem> = listOf(
        PhonicsItem('A', "ant", FarmIconKey.ANT),
        PhonicsItem('B', "barn", FarmIconKey.BARN),
        PhonicsItem('C', "cow", FarmIconKey.COW),
        PhonicsItem('D', "duck", FarmIconKey.DUCK),
        PhonicsItem('E', "egg", FarmIconKey.EGG),
        PhonicsItem('F', "frog", FarmIconKey.FROG),
        PhonicsItem('G', "goat", FarmIconKey.GOAT),
        PhonicsItem('H', "hen", FarmIconKey.HEN),
        PhonicsItem('M', "milk", FarmIconKey.MILK),
        PhonicsItem('O', "okra", FarmIconKey.OKRA),
        PhonicsItem('P', "pig", FarmIconKey.PIG),
        PhonicsItem('R', "rice", FarmIconKey.RICE),
        PhonicsItem('S', "sun", FarmIconKey.SUN),
        PhonicsItem('W', "wind", FarmIconKey.WIND)
    )

    val blendWords: List<BlendWord> = listOf(
        BlendWord("cat", FarmIconKey.CAT),
        BlendWord("dog", FarmIconKey.DOG),
        BlendWord("hen", FarmIconKey.HEN),
        BlendWord("mud", FarmIconKey.MUD),
        BlendWord("pig", FarmIconKey.PIG),
        BlendWord("rat", FarmIconKey.RAT),
        BlendWord("sun", FarmIconKey.SUN),
        BlendWord("wet", FarmIconKey.WET),
        BlendWord("corn", FarmIconKey.CORN),
        BlendWord("duck", FarmIconKey.DUCK),
        BlendWord("goat", FarmIconKey.GOAT),
        BlendWord("rice", FarmIconKey.RICE)
    )

    val patternWords: List<PatternWord> = listOf(
        PatternWord("chick", "CH", FarmIconKey.CHICK),
        PatternWord("chicken", "CH", FarmIconKey.CHICKEN),
        PatternWord("sheep", "SH", FarmIconKey.SHEEP),
        PatternWord("shed", "SH", FarmIconKey.SHED),
        PatternWord("shovel", "SH", FarmIconKey.SHOVEL),
        PatternWord("duck", "CK", FarmIconKey.DUCK),
        PatternWord("bee", "EE", FarmIconKey.BEE),
        PatternWord("seed", "EE", FarmIconKey.SEED),
        PatternWord("seeds", "EE", FarmIconKey.SEEDS),
        PatternWord("bean", "EA", FarmIconKey.BEAN),
        PatternWord("rain", "AI", FarmIconKey.RAIN),
        PatternWord("goat", "OA", FarmIconKey.GOAT),
        PatternWord("grain", "GR", FarmIconKey.GRAIN),
        PatternWord("straw", "ST", FarmIconKey.STRAW),
        PatternWord("spade", "SP", FarmIconKey.SPADE)
    )
}
