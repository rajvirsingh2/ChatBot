package com.example.chatbot.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class ChatRepository {
    private val db= FirebaseFirestore.getInstance()
    private val userId="test"

    fun getConversation(): Flow<List<Conversation>>{
        return db.collection("users").document(userId)
            .collection("conversations")
            .orderBy("lastUpdated", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot -> snapshot.toObjects() }
    }

    suspend fun createNewConvo(): String {
        val newConversation = Conversation(title = "New Chat")
        val documentRef = db.collection("users").document(userId)
            .collection("conversations").add(newConversation).await()
        return documentRef.id
    }

    fun getChatMessages(conversationId: String): Flow<List<ChatMessage>> {
        return db.collection("users").document(userId)
            .collection("conversations").document(conversationId)
            .collection("messages").orderBy("timestamp", Query.Direction.ASCENDING)
            .snapshots()
            .map { snapshot -> snapshot.toObjects() }
    }

    suspend fun addMessage(conversationId: String, message: ChatMessage) {
        db.collection("users").document(userId)
            .collection("conversations").document(conversationId)
            .collection("messages").add(message).await()

        db.collection("users").document(userId)
            .collection("conversations").document(conversationId)
            .update("lastUpdated", com.google.firebase.firestore.FieldValue.serverTimestamp())
            .await()
    }

    fun getChatMessagesFlow(conversationId: String): Flow<List<ChatMessage>> = callbackFlow {
        val listener = db.collection("users").document(userId)
            .collection("conversations").document(conversationId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    close(e)
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents?.mapNotNull { it.toObject(ChatMessage::class.java) } ?: emptyList()
                trySend(messages)
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateTitle(convoId: String, newTitle: String){
        db.collection("users").document(userId)
            .collection("conversations").document(convoId)
            .update("title",newTitle)
            .await()
    }

    fun getSingleConvo(convoId: String):Flow<Conversation?>{
        return db.collection("users").document(userId)
            .collection("conversations").document(convoId)
            .snapshots()
            .map { snapshot -> snapshot.toObject<Conversation>() }
    }
}