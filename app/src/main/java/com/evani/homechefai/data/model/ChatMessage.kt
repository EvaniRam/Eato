package com.evani.homechefai.data.model

import android.graphics.Bitmap

sealed class ChatMessage {
    data class User(val text: String, val image: Bitmap? = null) : ChatMessage()
    data class Assistant(val text: String) : ChatMessage()
} 