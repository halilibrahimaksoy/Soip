package com.haksoy.soip.messaging

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.haksoy.soip.data.chat.Chat
import com.haksoy.soip.data.database.UserRepository
import com.haksoy.soip.data.message.ChatEventType
import com.haksoy.soip.data.message.MessageBody
import com.haksoy.soip.data.message.MessageChat
import com.haksoy.soip.data.message.MessageData
import com.haksoy.soip.data.message.MessageEventType
import com.haksoy.soip.data.message.MessageResponse
import com.haksoy.soip.data.message.PriorityAndroid
import com.haksoy.soip.data.message.PriorityIOS
import com.haksoy.soip.data.user.User
import com.haksoy.soip.utlis.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "SoIP:MessageRepository"

class MessageRepository(val userRepository: UserRepository) {

    private val firebaseAPIService = RetrofitService.api

    fun sendMessageToUser(
        to: String,
        userUid: String,
        remoteChat: Chat
    ): MutableLiveData<Resource<Boolean>> {
        val result = MutableLiveData<Resource<Boolean>>()
        firebaseAPIService.sendNotification(
            MessageBody(
                to,
                MessageData(
                    userUid,
                    MessageEventType.CHAT,
                    MessageChat(
                        ChatEventType.NEW,
                        remoteChat
                    )
                ),
                PriorityAndroid("high"),
                PriorityIOS(mapOf(Pair("apns-priority", "10")))
            )
        ).enqueue(object : Callback<MessageResponse> {
            override fun onResponse(
                call: Call<MessageResponse>,
                response: Response<MessageResponse>
            ) {
                response.body()?.let {
                    if (it.success == 1)
                        result.value = Resource.success(true)
                    else
                        result.value = Resource.error("")
                }


            }

            override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                result.value = Resource.error(t.localizedMessage)
            }
        })
        return result
    }

//    fun sendChat(
//        to: String,
//        userUid: String,
//        remoteChat: Chat
//    ): MutableLiveData<Resource<Exception>> {
//        var result = MutableLiveData<Resource<Exception>>()
//        if (ChatType.isMedia(remoteChat.type)) {
//            firebaseDao.uploadMedia(remoteChat.getFileName(), remoteChat.contentUrl!!).observeOnce {
//                if (it.status == Resource.Status.SUCCESS) {
//                    Log.i(TAG, "uploadMedia SUCCESS")
//                    remoteChat.contentUrl = it.data
//                    result = sendMessage(to, userUid, remoteChat)
//                } else if (it.status == Resource.Status.ERROR) {
//                    result.value = Resource.error(it.message!!)
//                }
//            }
//        } else {
//            return sendMessage(to, userUid, remoteChat)
//        }
//        return result
//    }

//    private fun sendMessage(
//        to: String,
//        userUid: String,
//        remoteChat: Chat,
//        tryCount: Int = 0
//    ): MutableLiveData<Resource<Exception>> {
//        Log.i(TAG, "sendMessage -> $remoteChat to -> $to ")
//        var result = MutableLiveData<Resource<Exception>>()
//        firebaseAPIService.sendNotification(
//            MessageBody(
//                to,
//                MessageData(
//                    userUid,
//                    MessageEventType.CHAT,
//                    MessageChat(
//                        ChatEventType.NEW,
//                        remoteChat
//                    )
//                ),
//                PriorityAndroid("high"),
//                PriorityIOS(mapOf(Pair("apns-priority", "10")))
//            )
//        ).enqueue(object : Callback<MessageResponse> {
//            override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
//                result.value = Resource.error(t.localizedMessage!!)
//            }
//
//            override fun onResponse(
//                call: Call<MessageResponse>,
//                response: Response<MessageResponse>
//            ) {
//                if (response.body()!!.success == 1)
//                    result.value = Resource.success(null)
//                if (response.body()!!.failure == 1 && tryCount < 1) {
//                    fetchUserData(userUid).observeOnce {
//                        if (it.status == Resource.Status.SUCCESS) {
//                            result = sendMessage(it.data!!, userUid, remoteChat, tryCount.inc())
//                        } else if (it.status == Resource.Status.ERROR) {
//                            result.value = Resource.error(it.message!!)
//                        }
//
//                    }
//                }
//            }
//
//        })
//        return result
//    }

    private fun addUser(user: User) {
        GlobalScope.launch(Dispatchers.IO) {
            userRepository.addUser(user)
        }
    }

    fun removeChat(
        to: String,
        userUid: String,
        remoteChat: Chat
    ): MutableLiveData<Resource<Exception>> {
        Log.i(TAG, "removeChat -> $remoteChat to -> $to ")
        val result = MutableLiveData<Resource<Exception>>()
//        firebaseAPIService.sendNotification(
//            MessageBody(
//                to,
//                MessageData(
//                    userUid,
//                    MessageEventType.CHAT,
//                    MessageChat(
//                        ChatEventType.DELETE,
//                        remoteChat
//                    )
//                )
//            )
//        ).enqueue(object : Callback<MessageResponse> {
//            override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
//                result.value = Resource.error(t.localizedMessage)
//            }
//
//            override fun onResponse(
//                call: Call<MessageResponse>,
//                response: Response<MessageResponse>
//            ) {
//                result.value = Resource.success(null)
//            }
//
//        })
        return result
    }
}