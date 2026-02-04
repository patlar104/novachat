package com.novachat.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.novachat.app.data.AiMode
import com.novachat.app.data.AiRepository
import com.novachat.app.data.ChatMessage
import com.novachat.app.data.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel for managing chat state and interactions
 */
class ChatViewModel(application: Application) : AndroidViewModel(application) {
    
    private val preferencesRepository = PreferencesRepository(application)
    private val aiRepository = AiRepository(application)
    
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _apiKey = MutableStateFlow<String?>(null)
    val apiKey: StateFlow<String?> = _apiKey.asStateFlow()
    
    private val _aiMode = MutableStateFlow(AiMode.ONLINE)
    val aiMode: StateFlow<AiMode> = _aiMode.asStateFlow()
    
    init {
        loadPreferences()
    }
    
    private fun loadPreferences() {
        viewModelScope.launch {
            preferencesRepository.apiKey.collect { key ->
                _apiKey.value = key
            }
        }
        
        viewModelScope.launch {
            preferencesRepository.aiMode.collect { mode ->
                _aiMode.value = mode
            }
        }
    }
    
    fun sendMessage(text: String) {
        if (text.isBlank()) return
        
        val userMessage = ChatMessage(text = text, isUser = true)
        _messages.value = _messages.value + userMessage
        _isLoading.value = true
        _error.value = null
        
        viewModelScope.launch {
            val currentMode = _aiMode.first()
            val currentApiKey = _apiKey.first()
            
            val result = when (currentMode) {
                AiMode.ONLINE -> {
                    if (currentApiKey.isNullOrBlank()) {
                        Result.failure(Exception("API key not set. Please configure it in Settings."))
                    } else {
                        aiRepository.sendMessageToGemini(text, currentApiKey)
                    }
                }
                AiMode.OFFLINE -> {
                    aiRepository.sendMessageToAiCore(text)
                }
            }
            
            result.fold(
                onSuccess = { responseText ->
                    val aiMessage = ChatMessage(text = responseText, isUser = false)
                    _messages.value = _messages.value + aiMessage
                },
                onFailure = { exception ->
                    _error.value = exception.message ?: "An error occurred"
                }
            )
            
            _isLoading.value = false
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun clearChat() {
        _messages.value = emptyList()
    }
    
    fun saveApiKey(apiKey: String) {
        viewModelScope.launch {
            preferencesRepository.saveApiKey(apiKey)
        }
    }
    
    fun saveAiMode(mode: AiMode) {
        viewModelScope.launch {
            preferencesRepository.saveAiMode(mode)
        }
    }
}
