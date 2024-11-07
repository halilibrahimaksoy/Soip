package com.haksoy.soip.ui.conversationDetail

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.haksoy.soip.data.FirebaseDao
import com.haksoy.soip.data.chat.Chat
import com.haksoy.soip.data.chat.ChatDirection
import com.haksoy.soip.data.chat.ChatType
import com.haksoy.soip.data.chat.Status
import com.haksoy.soip.data.database.ChatRepository
import com.haksoy.soip.data.database.UserRepository
import com.haksoy.soip.data.user.User
import com.haksoy.soip.messaging.MessageRepository
import com.haksoy.soip.utlis.Resource
import com.haksoy.soip.utlis.observeOnce
import com.haksoy.soip.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID
import javax.inject.Inject

private const val TAG = "SoIP:ConversationDetailViewModel"

@HiltViewModel
class ConversationDetailViewModel @Inject constructor(
    app: Application,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    val firebaseDao: FirebaseDao
) : BaseViewModel(app) {
    private val messageRepository = MessageRepository(userRepository)

    lateinit var user: User
    lateinit var conversationDetailList: LiveData<List<Chat>>
    fun getConversationDetail(uid: String) {
        conversationDetailList = chatRepository.getConversationDetails(uid)
    }

    fun sendMessage(message: String) {
        Log.i(TAG, "sendChat -> $message")
        val localChat = createLocalChat(message)
        addLocalChat(localChat)
        val remoteChat = createRemoteChat(localChat)

        if (!user.token.isNullOrEmpty()) {
            sendChatUpdateUserIfNeeded(localChat, remoteChat, user)
        }
    }

    fun sendMedia(fileName: String, filePath: String, chatType: ChatType) {
        val localChat = createLocalChatForMedia(fileName, filePath, chatType)
        addLocalChat(localChat)
        firebaseDao.uploadMedia(getFileNameWithUserUid(fileName), filePath).observeOnce { it ->
            if (it.status == Resource.Status.SUCCESS) {
                val remoteChat = createRemoteChatForMedia(localChat, it.data!!)
                if (!user.token.isNullOrEmpty()) {
                    sendChatUpdateUserIfNeeded(localChat, remoteChat, user)
                }
            } else if (it.status == Resource.Status.ERROR) {
                errorMessages.postValue(it.message)
            }
        }
    }

    private fun sendChatUpdateUserIfNeeded(
        localChat: Chat,
        remoteChat: Chat,
        user: User,
        tryCount: Int = 0
    ) {
        if (tryCount < 2) {
            sendMessageToUser(remoteChat, user).observeOnce {
                if (it.status == Resource.Status.SUCCESS) {
                    updateChatStatus(localChat.uid, Status.SENT)
                } else if (it.status == Resource.Status.ERROR) {
                    updateUser(localChat, remoteChat, user, tryCount)
                }
            }
        }
    }

    private fun updateUser(
        localChat: Chat,
        remoteChat: Chat,
        user: User,
        tryCount: Int
    ) {
        firebaseDao.fetchUserDate(user.uid).observeOnce {
            if (it.status == Resource.Status.SUCCESS) {
                addUser(it.data!!)
                viewModelScope.launch {
                    sendChatUpdateUserIfNeeded(
                        localChat,
                        remoteChat,
                        user,
                        tryCount.inc()
                    )
                }

            } else if (it.status == Resource.Status.ERROR) {
                errorMessages.postValue(it.message)
            }
        }
    }


    private fun sendMessageToUser(remoteChat: Chat, user: User) =
        messageRepository.sendMessageToUser(user.token.toString(), user.uid, remoteChat)


    private fun addUser(user: User) {
        userRepository.addUser(user)
    }

    private fun createRemoteChat(
        localChat: Chat
    ) = Chat(
        localChat.uid,
        firebaseDao.getCurrentUserUid(),
        ChatDirection.InComing,
        false,
        ChatType.RECEIVED_TEXT,
        localChat.getText(),
        null,
        localChat.createDate,
        null, Status.SENT
    )

    private fun createRemoteChatForMedia(
        localChat: Chat, newFilePath: String
    ) = Chat(
        localChat.uid,
        firebaseDao.getCurrentUserUid(),
        ChatDirection.InComing,
        false,
        getRemoteMediaType(localChat.type),
        localChat.getText(),
        newFilePath,
        localChat.createDate,
        null, Status.SENT
    )

    private fun createLocalChat(message: String) = Chat(
        UUID.randomUUID().toString(),
        user.uid,
        ChatDirection.OutGoing,
        true,
        ChatType.SEND_TEXT,
        message,
        null,
        Date().time,
        null
    )

    private fun createLocalChatForMedia(fileName: String, filePath: String, chatType: ChatType) =
        Chat(
            UUID.randomUUID().toString(),
            user.uid,
            ChatDirection.OutGoing,
            true,
            chatType,
            fileName,
            filePath,
            Date().time,
            null
        )


    private fun addLocalChat(localChat: Chat) {
        chatRepository.addChat(localChat)
    }

    private fun updateChatStatus(uid: String, newStatus: Status) {
        chatRepository.updateChatStatus(uid, newStatus)
    }


    private fun getRemoteMediaType(chatType: ChatType): ChatType {
        return when (chatType) {
            ChatType.SEND_IMAGE -> ChatType.RECEIVED_IMAGE
            else -> ChatType.RECEIVED_VIDEO
        }
    }

    fun removeChatAtPosition(position: Int) {
        chatRepository.removeChat(conversationDetailList.value!![position])
    }

    fun getChatDirection(position: Int): ChatDirection =
        conversationDetailList.value!![position].direction

    fun sendRemoveRequestAtPosition(position: Int) {
        messageRepository.removeChat(
            user.token.toString(), user.uid,
            conversationDetailList.value!![position]
        ).observeOnce {
            if (it.status == Resource.Status.ERROR)
                errorMessages.postValue(it.message)
        }
    }

    fun markAsReadChat() {
        chatRepository.markAsRead(user.uid)
    }

    private fun getFileNameWithUserUid(fileName: String): String {
        return firebaseDao.getCurrentUserUid() + "_" + fileName
    }
}
