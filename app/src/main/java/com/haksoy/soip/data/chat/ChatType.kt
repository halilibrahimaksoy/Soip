package com.haksoy.soip.data.chat

import com.haksoy.soip.MainApplication
import com.haksoy.soip.R

enum class ChatType {
    SEND_TEXT,
    RECEIVED_TEXT,
    SEND_IMAGE,
    RECEIVED_IMAGE,
    SEND_VIDEO,
    TEXT,
    RECEIVED_VIDEO;

    companion object {
        fun isMedia(type: ChatType): Boolean {
            return !(type == ChatType.SEND_TEXT || type == ChatType.RECEIVED_TEXT)
        }

        fun getChatEmoji(type: ChatType): String {
            return when (type) {
                SEND_IMAGE,
                RECEIVED_IMAGE -> "\uD83D\uDCF7"
                SEND_VIDEO,
                RECEIVED_VIDEO -> "\uD83D\uDCF9"
                else -> ""
            }
        }

        fun getMediaText(type: ChatType): String {
            return when (type) {
                SEND_IMAGE,
                RECEIVED_IMAGE -> MainApplication.instance.getString(R.string.image)
                SEND_VIDEO, RECEIVED_VIDEO -> MainApplication.instance.getString(R.string.video)
                else -> ""
            }
        }
    }
}
