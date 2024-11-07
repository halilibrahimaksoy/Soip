package com.haksoy.soip.data.message

import com.haksoy.soip.data.chat.Chat

data class MessageChat(val chatEventType: ChatEventType, val chat: Chat)

enum class ChatEventType {
    NEW, DELETE
}