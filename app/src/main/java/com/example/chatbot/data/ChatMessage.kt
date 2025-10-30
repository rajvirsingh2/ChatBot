package com.example.chatbot.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class ChatMessage @JvmOverloads constructor(
    val text: String="",
    val participant: Role=Role.USER,
    @ServerTimestamp val timestamp: Timestamp? = null
)