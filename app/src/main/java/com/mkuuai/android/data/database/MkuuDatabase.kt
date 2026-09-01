package com.mkuuai.android.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mkuuai.android.data.model.Conversation
import com.mkuuai.android.data.model.Message

@Database(
    entities = [Conversation::class, Message::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MkuuDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile
        private var INSTANCE: MkuuDatabase? = null

        fun getDatabase(context: Context): MkuuDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MkuuDatabase::class.java,
                    "mkuuai.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mkuuai.android.data.model.Source

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromSourceList(sources: List<Source>): String {
        return gson.toJson(sources)
    }

    @TypeConverter
    fun toSourceList(json: String): List<Source> {
        val type = object : TypeToken<List<Source>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
}
