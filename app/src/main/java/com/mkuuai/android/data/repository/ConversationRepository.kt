package com.mkuuai.android.data.repository

import com.mkuuai.android.data.database.ConversationDao
import com.mkuuai.android.data.database.MessageDao
import com.mkuuai.android.data.model.Conversation
import com.mkuuai.android.data.model.Message
import kotlinx.coroutines.flow.Flow

class ConversationRepository(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao
) {
    fun getAllConversations(): Flow<List<Conversation>> {
        return conversationDao.getAllConversations()
    }

    suspend fun getConversationById(id: String): Conversation? {
        return conversationDao.getConversationById(id)
    }

    suspend fun createConversation(): Conversation {
        val conversation = Conversation()
        conversationDao.insert(conversation)
        return conversation
    }

    suspend fun updateConversationTitle(id: String, title: String) {
        val conversation = conversationDao.getConversationById(id) ?: return
        conversationDao.update(
            conversation.copy(
                title = title,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteConversation(id: String) {
        val conversation = conversationDao.getConversationById(id) ?: return
        messageDao.deleteByConversationId(id)
        conversationDao.delete(conversation)
    }

    fun getMessagesByConversation(conversationId: String): Flow<List<Message>> {
        return messageDao.getMessagesByConversation(conversationId)
    }

    suspend fun addMessage(message: Message) {
        messageDao.insert(message)
    }

    suspend fun updateMessage(message: Message) {
        messageDao.update(message)
    }
}
