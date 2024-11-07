package com.haksoy.soip.messaging

import android.content.Context
import android.media.RingtoneManager
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.haksoy.soip.MainApplication
import com.haksoy.soip.data.FirebaseDao
import com.haksoy.soip.data.chat.Chat
import com.haksoy.soip.data.chat.ChatType
import com.haksoy.soip.data.database.AppDatabase
import com.haksoy.soip.data.database.ChatRepository
import com.haksoy.soip.data.database.UserRepository
import com.haksoy.soip.data.message.ChatEventType
import com.haksoy.soip.data.message.MessageChat
import com.haksoy.soip.utlis.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class MessageHandler @Inject constructor(
    @ApplicationContext val context: Context,
    private val notificationHelper: NotificationHelper,
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val firebaseDao: FirebaseDao
) {


    fun handleChatNotificationData(messageData: MessageChat) {
        when (messageData.chatEventType) {
            ChatEventType.NEW -> {
                handleNewChat(messageData.chat)
            }

            ChatEventType.DELETE -> {
                handleRemoveChat(messageData.chat)
            }
        }
    }

    private fun handleRemoveChat(chat: Chat) {
        GlobalScope.launch(Dispatchers.IO) {
            chatRepository.removeChat(chat)
        }
        notificationHelper.removeNotification(chat.userUid)
    }

    private fun handleNewChat(chat: Chat) {
        GlobalScope.launch(Dispatchers.Main) {
            saveNewChat(chat).observeOnce {
                sendNotification(chat)
            }
        }
    }

    private fun sendNotification(chat: Chat) {
        GlobalScope.launch(Dispatchers.Main) {
            userRepository.getUser(chat.userUid).observeOnce {
                if (!context.isAppInForeground()) {
                    if (it.status == Resource.Status.SUCCESS) {
                        notificationHelper
                            .sendNotification(it.data!!)
                    }
                } else {
                    try {
                        val notification: Uri =
                            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                        val r = RingtoneManager.getRingtone(
                            context,
                            notification
                        )
                        r.play()
                        MainApplication.instance.vibratePhone()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun saveNewChat(chat: Chat): LiveData<Resource<Boolean>> {
        val result = MutableLiveData<Resource<Boolean>>()
        if (ChatType.isMedia(chat.type)) {
            val desFile = FileUtils.generateFile(ChatType.SEND_IMAGE)
            GlobalScope.launch(Dispatchers.Main) {
                firebaseDao.getImage(chat.getText(), desFile!!.absolutePath)
                    .observeOnce {
                        if (it.status == Resource.Status.SUCCESS) {
                            chat.contentUrl = desFile.absolutePath
                            addChat(chat)
                            result.value = it
                        } else if (it.status == Resource.Status.ERROR) {
                            //todo handle donwload error
                            addChat(chat)
                            result.value = it
                        }
                    }
            }
        } else {
            addChat(chat)
            result.value = Resource.success(null)
        }
        return result
    }

    private fun addChat(chat: Chat) {
        GlobalScope.launch(Dispatchers.Main) {
            chatRepository.addChat(chat)
        }

    }
}