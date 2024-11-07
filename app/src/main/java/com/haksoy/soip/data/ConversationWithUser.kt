package com.haksoy.soip.data

import androidx.room.Embedded
import com.haksoy.soip.data.chat.Conversation
import com.haksoy.soip.data.user.User

data class ConversationWithUser(
    @Embedded
    val conversation: Conversation? = null,
    @Embedded
    val user: User? = null
)