package com.mkuuai.android.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.mkuuai.android.data.model.Message
import com.mkuuai.android.ui.component.AiMessage
import com.mkuuai.android.ui.component.EmptyState
import com.mkuuai.android.ui.component.ErrorState
import com.mkuuai.android.ui.component.MessageComposer
import com.mkuuai.android.ui.component.MkuuTopBar
import com.mkuuai.android.ui.component.UserMessage
import com.mkuuai.android.ui.viewmodel.ChatViewModel

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var messageInput by remember { mutableStateOf("") }
    var showDrawer by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            MkuuTopBar(
                title = "Chat",
                onMenuClick = { showDrawer = true },
                onNewConversation = { viewModel.createNewConversation() }
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Messages
                if (uiState.messages.isEmpty() && !uiState.isGenerating) {
                    EmptyState(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        state = listState
                    ) {
                        items(uiState.messages) { message ->
                            when (message.role) {
                                "user" -> UserMessage(content = message.content)
                                "assistant" -> AiMessage(
                                    content = message.content,
                                    isStreaming = false
                                )
                            }
                        }

                        if (uiState.isGenerating && uiState.currentResponse.isNotEmpty()) {
                            item {
                                AiMessage(
                                    content = uiState.currentResponse,
                                    isStreaming = true
                                )
                            }
                        }
                    }
                }

                // Error handling
                if (uiState.error != null) {
                    ErrorState(
                        message = uiState.error!!,
                        onRetry = { /* Implement retry logic */ }
                    )
                }

                // Message composer
                MessageComposer(
                    message = messageInput,
                    onMessageChange = { messageInput = it },
                    onSend = {
                        viewModel.sendMessage(messageInput)
                        messageInput = ""
                    },
                    onStop = if (uiState.isGenerating) {
                        { viewModel.stopGeneration() }
                    } else null,
                    isGenerating = uiState.isGenerating,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
