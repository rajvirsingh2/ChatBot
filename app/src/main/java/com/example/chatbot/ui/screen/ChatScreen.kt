package com.example.chatbot.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.chatbot.data.ChatMessage
import com.example.chatbot.data.Role
import com.example.chatbot.ui.models.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    conversationId: String,
    viewModel: ChatViewModel = viewModel(
        factory = ChatViewModelFactory(conversationId)
    ),
    onNavigateBack: ()-> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var userInput by remember { mutableStateOf("") }
    var isEditingTitle by remember { mutableStateOf(false) }
    var editableTitle by remember { mutableStateOf("") }

    LaunchedEffect(uiState.conversation) {
        editableTitle=uiState.conversation?.title ?: "New Chat"
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if(isEditingTitle){
                        BasicTextField(
                            editableTitle,
                            onValueChange = {editableTitle=it},
                            textStyle = TextStyle(color = MaterialTheme.colorScheme.onPrimary)
                        )
                    }else{
                        Text(uiState.conversation?.title ?: "Loading...")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {onNavigateBack()}) {
                        Icon(Icons.Filled.ArrowBack,"Back")
                    }
                },

                actions = {
                    if(isEditingTitle){
                        IconButton(onClick = {
                            viewModel.updateTitle(editableTitle)
                            isEditingTitle=false
                        }) {
                            Icon(Icons.Filled.Done, "Done")
                        }
                    }else{
                        IconButton(onClick = {isEditingTitle=true}) {
                            Icon(Icons.Filled.Edit, "Edit Title")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...") }
                )
                IconButton(onClick = {
                    if (userInput.isNotBlank()) {
                        viewModel.sendMessage(userInput)
                        userInput = ""
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send"
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 8.dp),
            reverseLayout = true
        ) {
            items(uiState.message.asReversed(), key = {it.timestamp?.toString() ?: it.text}) { message: ChatMessage ->
                MessageBubble(message)
            }
        }
    }
}

@Composable
fun MessageBubble(message: ChatMessage) {
    val isUserMessage = message.participant == Role.USER
    val horizontalArrangement = if (isUserMessage) Arrangement.End else Arrangement.Start

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if(message.participant== Role.USER) Arrangement.End else Arrangement.Start
    ) {
        val bubbleColor = when (message.participant) {
            Role.USER -> MaterialTheme.colorScheme.primaryContainer
            Role.MODEL -> MaterialTheme.colorScheme.secondaryContainer
            Role.ERROR -> MaterialTheme.colorScheme.errorContainer
        }
        Card(
            modifier = Modifier.widthIn(max = 300.dp),
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = bubbleColor)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

class ChatViewModelFactory(private val conversationId: String) :
    androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(conversationId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}