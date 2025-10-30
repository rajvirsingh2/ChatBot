package com.example.chatbot.ui.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatbot.data.ChatRepository
import com.example.chatbot.data.Conversation
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ConversationsViewModel : ViewModel() {
    private val repository = ChatRepository()

    val conversations: StateFlow<List<Conversation>> = repository.getConversation().stateIn(viewModelScope,
        SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun createNewConversation(): String {
        return repository.createNewConvo()
    }
}