package com.sgbread.app.data

object Praise {
    val correct = listOf("Great Job!", "Excellent!", "Amazing!", "Well Done!", "Keep Going!", "Super Reader!")
    val encouragement = listOf("Try again, you can do it!", "Almost there, try once more!", "Good try! Let's try again.")

    fun randomCorrect(): String = correct.random()
    fun randomEncouragement(): String = encouragement.random()
}
