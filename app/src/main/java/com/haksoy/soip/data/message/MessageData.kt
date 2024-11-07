package com.haksoy.soip.data.message

data class MessageData(
    val destinationUid: String,
    val messageEventType: MessageEventType,
    val content: Any
)

enum class MessageEventType {
    CHAT, INFORMATION
}