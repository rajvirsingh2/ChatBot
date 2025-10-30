package com.example.chatbot.ui.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatbot.data.ChatMessage
import com.example.chatbot.data.ChatRepository
import com.example.chatbot.data.Conversation
import com.example.chatbot.data.Role
import com.google.firebase.Firebase
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.ai
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.content
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class ChatUiState(
    val conversation: Conversation?=null,
    val message: List<ChatMessage> = emptyList()
)

@Suppress("UNCHECKED_CAST")
class ChatViewModel(private val convoId: String): ViewModel(){
    private val repository = ChatRepository()

    private val _uiState: MutableStateFlow<ChatUiState> = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = combine(
        repository.getSingleConvo(convoId),
        repository.getChatMessages(convoId)
    ) { conversation, messages ->
        ChatUiState(conversation, messages)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = ChatUiState()
    )

    fun updateTitle(newTitle: String) {
        viewModelScope.launch {
            repository.updateTitle(convoId, newTitle)
        }
    }
    private val genModel: GenerativeModel = Firebase.ai.generativeModel(
        modelName = "gemini-2.5-flash"
    )

    init {
        observeMessages()
    }
    private fun observeMessages() {
        viewModelScope.launch {
            repository.getChatMessagesFlow(convoId).collect { messages ->
                _uiState.update { it.copy(message = messages) }
            }
        }
    }

    fun sendMessage(userInput: String) {
        val userMessage = ChatMessage(text = userInput, participant = Role.USER)
        viewModelScope.launch {
            repository.addMessage(convoId, userMessage)

            val chatHistory = _uiState.value.message.map { msg ->
                content(role = if (msg.participant == Role.USER) "user" else "model") {
                    text(msg.text)
                }
            }

            val chat = genModel.startChat(history = chatHistory)

            try {
                val response = chat.sendMessage(userInput)
                response.text?.let { modelResponse ->
                    val modelMessage = ChatMessage(text = modelResponse, participant = Role.MODEL)
                    repository.addMessage(convoId, modelMessage)
                }
            } catch (e: Exception) {
                val errorMessage = ChatMessage(
                    text = e.localizedMessage ?: "Something went wrong",
                    participant = Role.ERROR
                )
                repository.addMessage(convoId, errorMessage)
            }
        }
    }

}