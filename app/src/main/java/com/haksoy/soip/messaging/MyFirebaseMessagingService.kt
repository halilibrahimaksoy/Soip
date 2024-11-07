package com.haksoy.soip.messaging

import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.haksoy.soip.BuildConfig
import com.haksoy.soip.data.FirebaseDao
import com.haksoy.soip.data.message.MessageChat
import com.haksoy.soip.data.message.MessageEventType
import com.haksoy.soip.utlis.Constants
import com.haksoy.soip.utlis.JsonUtil
import com.haksoy.soip.utlis.getPreferencesString
import com.haksoy.soip.utlis.putPreferencesString
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {
    @Inject
    lateinit var messageHandler: MessageHandler

    @Inject
    lateinit var firebaseDao: FirebaseDao
    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)

        if (BuildConfig.DEBUG) {
            Handler(Looper.getMainLooper()).post(Runnable {
                Toast.makeText(applicationContext, "New Message !", Toast.LENGTH_SHORT).show()
            })
        }

        if (p0.data.containsKey(Constants.MESSAGE_EVENT_TYPE)) {
            val messageData = JsonUtil.getMessageData(p0.data.toString())
            if (messageData.destinationUid == getPreferencesString(Constants.USER_UID, "")) {
                when (messageData.messageEventType) {
                    MessageEventType.CHAT -> {
                        messageHandler.handleChatNotificationData(messageData.content as MessageChat)
                    }

                    else -> {}
                }
            }
        }
    }


    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        if (firebaseDao.isAuthUserExist())
            firebaseDao.updateToken(p0)
        putPreferencesString(Constants.FIREBASE_MESSAGING_TOKEN, p0)
    }

    companion object {

        private const val TAG = "MyFirebaseMsgService"
    }
}