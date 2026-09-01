package com.mkuuai.android.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.mkuuai.android.ui.theme.MkuuAITheme
import com.mkuuai.android.ui.screen.ChatScreen
import com.mkuuai.android.data.repository.ConversationRepository
import com.mkuuai.android.data.repository.ChatRepository
import com.mkuuai.android.data.database.MkuuDatabase
import com.mkuuai.android.data.api.ChatApiClient
import com.mkuuai.android.ui.viewmodel.ChatViewModel
import com.mkuuai.android.ui.viewmodel.ChatViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize database and repositories
        val database = MkuuDatabase.getDatabase(this)
        val conversationDao = database.conversationDao()
        val messageDao = database.messageDao()
        
        val conversationRepository = ConversationRepository(conversationDao, messageDao)
        val chatRepository = ChatRepository(ChatApiClient())
        
        val factory = ChatViewModelFactory(conversationRepository, chatRepository)
        val viewModel = ViewModelProvider(this, factory)[ChatViewModel::class.java]

        setContent {
            MkuuAITheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ChatScreen(viewModel = viewModel)
                }
            }
        }
    }
}
