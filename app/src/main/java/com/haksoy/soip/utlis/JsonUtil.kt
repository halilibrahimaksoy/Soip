package com.haksoy.soip.utlis

import com.haksoy.soip.data.chat.Chat
import com.haksoy.soip.data.chat.ChatDirection
import com.haksoy.soip.data.chat.ChatType
import com.haksoy.soip.data.message.ChatEventType
import com.haksoy.soip.data.message.MessageChat
import com.haksoy.soip.data.message.MessageData
import com.haksoy.soip.data.message.MessageEventType
import org.json.JSONObject

object JsonUtil {
    fun getMessageData(jsonString: String): MessageData {
        val jsonObject = JSONObject(jsonString)


        val destinationUid = jsonObject[Constants.DESTINATION_UID].toString()
        return when (val messageEventType =
            MessageEventType.valueOf(jsonObject[Constants.MESSAGE_EVENT_TYPE].toString())) {
            MessageEventType.CHAT -> {
                val messageChat = getMessageChat(jsonObject[Constants.CONTENT].toString())
                MessageData(destinationUid, messageEventType, messageChat)
            }
            else ->
                MessageData(destinationUid, messageEventType, false)
        }

    }

    private fun getMessageChat(jsonString: String): MessageChat {
        val jsonObject = JSONObject(jsonString)

        val chatEventType = ChatEventType.valueOf(jsonObject[Constants.CHAT_EVENT_TYPE].toString())
        val chat = getChat(jsonObject[Constants.CHAT].toString())

        return MessageChat(chatEventType, chat)
    }

    private fun getChat(jsonString: String): Chat {
        val jsonObject = JSONObject(jsonString)

        val uid = jsonObject[Constants.UID].toString()
        val userUid = jsonObject[Constants.USER_UID].toString()
        val direction = ChatDirection.valueOf(jsonObject[Constants.DIRECTION].toString())
        val isSeen = jsonObject[Constants.IS_SEEN] as Boolean
        val type = ChatType.valueOf(jsonObject[Constants.TYPE].toString())
        val createDate = jsonObject[Constants.CREATE_DATE] as Long
        val text =
            if (jsonObject.has(Constants.TEXT)) jsonObject[Constants.TEXT].toString() else null
        val updateDate =
            if (jsonObject.has(Constants.UPDATE_DATE)) jsonObject[Constants.UPDATE_DATE] as Long else null
        val contentUrl =
            if (jsonObject.has(Constants.CONTENT_URL)) jsonObject[Constants.CONTENT_URL].toString() else null

        return Chat(uid, userUid, direction, isSeen, type, text, contentUrl, createDate, updateDate)
    }

}