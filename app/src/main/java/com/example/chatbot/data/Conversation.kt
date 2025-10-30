package com.example.chatbot.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class Conversation(
    @DocumentId val id: String = "",
    val title: String = "New Chat",
    @ServerTimestamp val lastMessage: Timestamp? = null
)