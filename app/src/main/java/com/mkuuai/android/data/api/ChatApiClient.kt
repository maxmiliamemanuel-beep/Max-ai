package com.mkuuai.android.data.api

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.util.concurrent.TimeUnit

class ChatApiClient(
    private val baseUrl: String = "https://api.mkuuai.com"
) {
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    fun streamChat(
        request: ChatRequest,
        onCancel: (() -> Unit)? = null
    ): Flow<StreamEvent> = flow {
        var call: okhttp3.Call? = null
        try {
            val requestBody = okhttp3.RequestBody.create(
                okhttp3.MediaType.parse("application/json"),
                kotlinx.serialization.json.Json.encodeToString(request)
            )

            val httpRequest = Request.Builder()
                .url("$baseUrl/api/v1/chat/stream")
                .post(requestBody)
                .addHeader("Accept", "text/event-stream")
                .addHeader("Content-Type", "application/json")
                .build()

            call = httpClient.newCall(httpRequest)
            val response = call.execute()

            if (!response.isSuccessful) {
                emit(StreamEvent(
                    type = "error",
                    data = "Failed: ${response.code}"
                ))
                return@flow
            }

            response.body()?.use { body ->
                body.charStream().useLines { lines ->
                    for (line in lines) {
                        if (line.isEmpty() || line.startsWith(":")) continue
                        
                        if (line.startsWith("data: ")) {
                            val data = line.substring(6)
                            try {
                                val event = parseEvent(data)
                                emit(event)
                            } catch (e: Exception) {
                                Timber.e(e, "Failed to parse event")
                            }
                        }
                    }
                }
            }
            emit(StreamEvent(type = "done"))
        } catch (e: Exception) {
            Timber.e(e, "Stream error")
            emit(StreamEvent(
                type = "error",
                data = "Connection error"
            ))
        } finally {
            onCancel?.invoke()
        }
    }

    fun cancel(call: okhttp3.Call?) {
        call?.cancel()
    }

    private fun parseEvent(data: String): StreamEvent {
        return try {
            StreamEvent(
                type = "text",
                data = data
            )
        } catch (e: Exception) {
            Timber.e(e, "Parse error: $data")
            StreamEvent(type = "error", data = "Parse error")
        }
    }
}
