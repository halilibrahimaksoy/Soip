package com.haksoy.soip.data.chat

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.haksoy.soip.data.ConversationWithUser

@Dao
interface ConversationDao {
    @Query("SELECT * FROM CONVERSATION_TABLE CONVERSATION INNER JOIN USER_TABLE  USER ON CONVERSATION.userUid == USER.uid ORDER BY CONVERSATION.chatCreateDate DESC")
    fun getConversationWithUserList(): LiveData<List<ConversationWithUser>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addConversation(conversation: Conversation)

    @Query("DELETE FROM conversation_table WHERE userUid=(:userUid)")
    fun removeConversation(userUid: String)

    @Query("UPDATE conversation_table SET text = null WHERE chatUid=(:chatUid)")
    fun removeChat(chatUid: String)

    @Query("UPDATE conversation_table SET status = (:newStatus) WHERE chatUid=(:chatUid)")
    fun updateChatStatus(chatUid: String, newStatus: Status)

    @Query("UPDATE conversation_table SET is_seen=1 WHERE userUid=(:userUid) and is_seen=0")
    fun markAsRead(userUid: String)
}