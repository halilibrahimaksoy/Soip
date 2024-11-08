package com.haksoy.soip.data.database

import android.util.Log
import androidx.lifecycle.LiveData
import com.haksoy.soip.data.chat.Chat
import com.haksoy.soip.data.chat.Conversation
import com.haksoy.soip.data.chat.Status
import javax.inject.Inject

private const val TAG = "ChatRepository"

class ChatRepository @Inject constructor(
    appDatabase: AppDatabase
) {

    private val chatDao = appDatabase.chatDao()
    private val conversationDao = appDatabase.conversationDao()

    fun getConversationWithUserList() =
        conversationDao.getConversationWithUserList()

    fun getConversationDetails(uid: String): LiveData<List<Chat>> =
        chatDao.getConversationDetails(uid)

    fun getConversationMedia(uid: String): LiveData<List<Chat>> =
        chatDao.getConversationMedia(uid)


    fun getUnreadConversation(uid: String): LiveData<List<Chat>> =
        chatDao.getUnreadConversation(uid)

    fun addChat(chatItem: Chat) {
        Log.i(TAG, "addChat -> $chatItem")
        chatDao.addChat(chatItem)
        conversationDao.addConversation(
            Conversation(
                chatItem.uid,
                chatItem.userUid,
                chatItem.direction,
                chatItem.is_seen,
                0,
                chatItem.type,
                chatItem.getText(),
                chatItem.createDate
            )
        )
    }

    fun updateChatStatus(uid: String, newStatus: Status) {
        chatDao.updateChatStatus(uid, newStatus)
        conversationDao.updateChatStatus(uid, newStatus)
    }

    fun removeChat(chatItem: Chat) {
        Log.i(TAG, "removeChat -> $chatItem")
        chatDao.removeChat(chatItem)
        conversationDao.removeChat(chatItem.uid)

    }

    fun removeConversation(userUid: String) {
        Log.i(TAG, "removeConversation -> $userUid")
        chatDao.removeConversation(userUid)
        conversationDao.removeConversation(userUid)
    }

    fun markAsRead(userUid: String) {
        Log.i(TAG, "markAsReadChat -> $userUid")
        chatDao.markAsRead(userUid)
        conversationDao.markAsRead(userUid)

    }
}