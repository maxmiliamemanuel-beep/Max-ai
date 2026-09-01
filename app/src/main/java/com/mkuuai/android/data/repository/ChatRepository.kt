package com.mkuuai.android.data.repository

import com.mkuuai.android.data.api.ChatApiClient
import com.mkuuai.android.data.api.ChatRequest
import com.mkuuai.android.data.api.StreamEvent
import kotlinx.coroutines.flow.Flow
import okhttp3.Call

class ChatRepository(
    private val apiClient: ChatApiClient
) {
    private var currentCall: Call? = null

    fun streamChat(
        request: ChatRequest,
        onCancel: (() -> Unit)? = null
    ): Flow<StreamEvent> {
        return apiClient.streamChat(request, onCancel)
    }

    fun cancelChat() {
        apiClient.cancel(currentCall)
        currentCall = null
    }
}
