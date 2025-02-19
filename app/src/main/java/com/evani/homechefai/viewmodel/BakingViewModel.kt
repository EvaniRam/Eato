package com.evani.homechefai.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.evani.homechefai.BuildConfig
import com.evani.homechefai.data.PreferencesManager
import com.evani.homechefai.data.model.UserPreferences
import com.evani.homechefai.ui.state.UiState
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class BakingViewModel(
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _uiState: MutableStateFlow<UiState> =
        MutableStateFlow(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.apiKey
    )

    private var currentJob: Job? = null

    // Add currentAssistantMessage to track the ongoing response
    private var currentAssistantMessage: ChatMessage.Assistant? = null

    private var userPreferences: UserPreferences? = null

    init {
        viewModelScope.launch {
            preferencesManager.userPreferencesFlow.collect { preferences ->
                userPreferences = preferences
            }
        }
    }

    fun updatePreferences(preferences: UserPreferences) {
        viewModelScope.launch {
            preferencesManager.updatePreferences(preferences)
        }
    }

    fun sendPrompt(
        bitmap: Bitmap?,
        prompt: String
    ) {
        currentJob?.cancel()
        
        if (prompt.isBlank()) return
        
        val userMessage = ChatMessage.User(prompt, bitmap)
        addMessage(userMessage)
        _uiState.value = UiState.Loading
        
        // Reset currentAssistantMessage for new chat
        currentAssistantMessage = null
        
        currentJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = generativeModel.generateContentStream(
                    content {
                        bitmap?.let { image(it) }
                        text(prompt)
                    }
                )

                var responseText = ""
                response.collect { chunk ->
                    chunk.text?.let { text ->
                        responseText += text
                        if (currentAssistantMessage == null) {
                            currentAssistantMessage = ChatMessage.Assistant(responseText)
                            addMessage(currentAssistantMessage!!)
                        } else {
                            currentAssistantMessage = ChatMessage.Assistant(responseText)
                            updateLastAssistantMessage(currentAssistantMessage!!)
                        }
                    }
                }
                
                _uiState.value = UiState.Success(responseText)
                // Reset currentAssistantMessage after successful response
                currentAssistantMessage = null
                
            } catch (e: Exception) {
                if (e is CancellationException) {
                    _uiState.value = UiState.Initial
                } else {
                    _uiState.value = UiState.Error(e.localizedMessage ?: "")
                    addMessage(ChatMessage.Assistant("Error: ${e.localizedMessage}"))
                }
                // Reset currentAssistantMessage on error
                currentAssistantMessage = null
            }
        }
    }

    private fun addMessage(message: ChatMessage) {
        _messages.value = _messages.value + message
    }

    private fun updateLastAssistantMessage(message: ChatMessage.Assistant) {
        val messages = _messages.value.toMutableList()
        val lastAssistantIndex = messages.indexOfLast { it is ChatMessage.Assistant }
        if (lastAssistantIndex >= 0) {
            messages[lastAssistantIndex] = message
            _messages.value = messages
        }
    }

    fun cancelPrompt() {
        currentJob?.cancel()
        currentJob = null
        _uiState.value = UiState.Initial
        // Clear current assistant message
        currentAssistantMessage = null
    }
}

sealed class ChatMessage {
    data class User(val text: String, val image: Bitmap? = null) : ChatMessage()
    data class Assistant(val text: String) : ChatMessage()
}