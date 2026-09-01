package com.mkuuai.android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mkuuai.android.data.model.Message
import com.mkuuai.android.data.repository.ChatRepository
import com.mkuuai.android.data.repository.ConversationRepository
import com.mkuuai.android.data.api.ChatRequest
import com.mkuuai.android.data.api.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

data class ChatUiState(
    val currentConversationId: String = "",
    val messages: List<Message> = emptyList(),
    val isGenerating: Boolean = false,
    val currentResponse: String = "",
    val error: String? = null
)

class ChatViewModel(
    private val conversationRepository: ConversationRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        createNewConversation()
    }

    fun createNewConversation() {
        viewModelScope.launch {
            try {
                val conversation = conversationRepository.createConversation()
                _uiState.value = _uiState.value.copy(
                    currentConversationId = conversation.id,
                    messages = emptyList()
                )
                observeMessages(conversation.id)
            } catch (e: Exception) {
                Timber.e(e, "Failed to create conversation")
                _uiState.value = _uiState.value.copy(
                    error = "Failed to create conversation"
                )
            }
        }
    }

    private fun observeMessages(conversationId: String) {
        viewModelScope.launch {
            conversationRepository.getMessagesByConversation(conversationId).collect { messages ->
                _uiState.value = _uiState.value.copy(messages = messages)
            }
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank() || _uiState.value.isGenerating) return

        viewModelScope.launch {
            try {
                val conversationId = _uiState.value.currentConversationId

                // Add user message
                val userMessage = Message(
                    conversationId = conversationId,
                    role = "user",
                    content = content
                )
                conversationRepository.addMessage(userMessage)

                // Start generation
                _uiState.value = _uiState.value.copy(
                    isGenerating = true,
                    currentResponse = "",
                    error = null
                )

                // Create chat request
                val chatMessages = _uiState.value.messages.map {
                    ChatMessage(role = it.role, content = it.content)
                }
                val chatRequest = ChatRequest(
                    conversationId = conversationId,
                    message = content,
                    context = chatMessages
                )

                // Stream response
                var fullResponse = ""
                chatRepository.streamChat(chatRequest).collect { event ->
                    when (event.type) {
                        "text" -> {
                            fullResponse += (event.data ?: "")
                            _uiState.value = _uiState.value.copy(
                                currentResponse = fullResponse
                            )
                        }
                        "done" -> {
                            // Save AI message
                            val aiMessage = Message(
                                conversationId = conversationId,
                                role = "assistant",
                                content = fullResponse
                            )
                            conversationRepository.addMessage(aiMessage)
                            _uiState.value = _uiState.value.copy(
                                isGenerating = false
                            )
                        }
                        "error" -> {
                            _uiState.value = _uiState.value.copy(
                                isGenerating = false,
                                error = event.data ?: "Generation failed"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to send message")
                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    error = "Failed to send message"
                )
            }
        }
    }

    fun stopGeneration() {
        chatRepository.cancelChat()
        _uiState.value = _uiState.value.copy(
            isGenerating = false
        )
    }
}

class ChatViewModelFactory(
    private val conversationRepository: ConversationRepository,
    private val chatRepository: ChatRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatViewModel(conversationRepository, chatRepository) as T
    }
}
