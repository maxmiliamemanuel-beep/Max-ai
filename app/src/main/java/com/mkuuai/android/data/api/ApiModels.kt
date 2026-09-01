package com.mkuuai.android.data.api

data class ChatRequest(
    val conversationId: String,
    val message: String,
    val context: List<ChatMessage> = emptyList()
)

data class ChatMessage(
    val role: String,
    val content: String
)

data class StreamEvent(
    val type: String, // "text", "source", "image", "done", "error"
    val data: String? = null
)

data class ImageGenerationRequest(
    val prompt: String,
    val conversationId: String
)

data class WebSearchRequest(
    val query: String,
    val conversationId: String
)
