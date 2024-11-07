package com.haksoy.soip.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.haksoy.soip.data.chat.*
import com.haksoy.soip.data.user.User
import com.haksoy.soip.data.user.UserDao

@Database(entities = [Chat::class, Conversation::class, User::class], version = 1)
@TypeConverters(ChatTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun conversationDao(): ConversationDao
    abstract fun userDao(): UserDao
}