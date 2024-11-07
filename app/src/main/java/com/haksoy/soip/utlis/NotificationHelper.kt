package com.haksoy.soip.utlis

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.haksoy.soip.MainApplication
import com.haksoy.soip.R
import com.haksoy.soip.data.chat.ChatType
import com.haksoy.soip.data.database.ChatRepository
import com.haksoy.soip.data.user.User
import com.haksoy.soip.ui.main.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class NotificationHelper @Inject constructor(@ApplicationContext val context: Context, private val chatRepository: ChatRepository) {
    private var manager: NotificationManager? = null
        get() {
            if (field == null) {
                field =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            }
            return field
        }

    fun sendNotification(user: User) {
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra(Constants.ConversationDetailFragmentSelectedUser, user.uid)
        val pendingIntent = PendingIntent.getActivity(
            context, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT
        )

        val channelId = Constants.NotificationChannelID
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setVibrate(longArrayOf(300, 300))
            .setContentIntent(pendingIntent)


        chatRepository.getUnreadConversation(user.uid).observeOnce {
            if (it.size > 1) {
                val inboxStyle = NotificationCompat.InboxStyle()
                for (chat in it) {
                    inboxStyle.addLine(chat.getText())
                }
                notificationBuilder.setStyle(inboxStyle)

            } else {
                val chat = it[0]
                if (ChatType.isMedia(chat.type)) {
                    val bigPictureStyle = NotificationCompat.BigPictureStyle()
                    bigPictureStyle.bigPicture(FileUtils.convertFileImageToBitmap(chat.contentUrl!!))
                    notificationBuilder.setStyle(bigPictureStyle)

                } else {
                    val bigTextStyle = NotificationCompat.BigTextStyle()
                    bigTextStyle.bigText(chat.getText())
                    notificationBuilder.setStyle(bigTextStyle)
                }
            }
            notificationBuilder.setContentTitle(
                getUserNameWithNumOfMessages(
                    it.size,
                    user.name!!
                )
            )
            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH
                )
                manager!!.createNotificationChannel(channel)
            }

            Glide.with(MainApplication.instance.applicationContext)
                .asBitmap()
                .circleCrop()
                .load(user.profileImage)
                .into(object : CustomTarget<Bitmap?>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap?>?
                    ) {
                        notificationBuilder.setLargeIcon(resource)
                        manager!!.notify(
                            user.uid.hashCode()/* ID of notification */,
                            notificationBuilder.build()
                        )
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        TODO("Not yet implemented")
                    }
                })
        }
    }

    private fun getUserNameWithNumOfMessages(
        unreadCount: Int,
        userName: String
    ): String {
        return if (unreadCount == 0 || unreadCount == 1) userName else "$userName ($unreadCount Messages) "
    }

    fun removeNotification(userUid: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(userUid.hashCode())
    }
}