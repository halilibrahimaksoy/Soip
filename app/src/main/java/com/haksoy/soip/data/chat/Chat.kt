package com.haksoy.soip.data.chat

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.haksoy.soip.utlis.Constants
import java.util.*

@Entity(tableName = Constants.CHAT_TABLE)
data class Chat(
    @PrimaryKey val uid: String = UUID.randomUUID().toString(),
    val userUid: String,
    val direction: ChatDirection,
    val is_seen: Boolean,
    val type: ChatType,
    private val text: String? = null,
    var contentUrl: String? = null,
    val createDate: Long,
    val updateDate: Long? = null,
    val status: Status? = Status.LOADING
) {

    override fun toString(): String {
        return if (direction == ChatDirection.InComing) "$text$contentUrl from  $userUid" else "$text$contentUrl to  $userUid"
    }

    fun getText(): String {
        return when (type) {
            ChatType.SEND_TEXT, ChatType.RECEIVED_TEXT -> text ?: ""
            else -> StringBuilder().append(ChatType.getChatEmoji(type)).append(" ")
                .append(ChatType.getMediaText(type)).toString()
        }
    }

    fun getFileName(): String {
        return text.toString()
    }

}



