package com.evani.homechefai.data.model

data class UserPreferences(
    val dietType: String = "",
    val cuisine: String = "",
    val country: String = "",
    val region: String = ""
)