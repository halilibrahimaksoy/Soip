package com.haksoy.soip.data.chat

import androidx.room.TypeConverter
import com.haksoy.soip.data.user.Location
import com.haksoy.soip.data.user.SocialMedia
import java.util.*

class ChatTypeConverters {
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun toDate(millisSinceEpoch: Long?): Date? {
        return millisSinceEpoch?.let {
            Date(it)
        }
    }

    @TypeConverter
    fun toChatType(value: String) = enumValueOf<ChatType>(value)

    @TypeConverter
    fun fromChatType(value: ChatType) = value.name

    @TypeConverter
    fun toChatDirection(value: String) = enumValueOf<ChatDirection>(value)

    @TypeConverter
    fun fromChatDirection(value: ChatDirection) = value.name

    @TypeConverter
    fun fromSocialMedia(socialMedia: SocialMedia): String {
        return socialMedia.toString()
    }

    @TypeConverter
    fun toSocialMedia(input: String): SocialMedia {
        return SocialMedia.toSocialMedia(input)
    }

    @TypeConverter
    fun fromLocation(location: Location): String {
        return "${location.latitude}:${location.longitude}"
    }

    @TypeConverter
    fun toLocation(string: String): Location {
        return Location(string.split(":")[0].toDouble(), string.split(":")[1].toDouble())
    }

    @TypeConverter
    fun toStatus(value: String) = enumValueOf<Status>(value)

    @TypeConverter
    fun fromStatus(status: Status) = status.name
}