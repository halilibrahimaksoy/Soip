package com.haksoy.soip.utlis

object Constants {
    const val User_Profile_Image = "USER_PROFILE_IMAGE"
    const val MEDIA = "MEDIA"

    //    val randomImageUrl = "https://loremflickr.com/320/240"
    const val randomImageUrl =
        "https://firebasestorage.googleapis.com/v0/b/voipapp-dbc05.appspot.com/o/USER_PROFILE_IMAGE%2Fdefault_image.jpg?alt=media&token=966a0ad1-3753-4c0f-a829-82281d1481cc"
    const val firebaseStoregeURL = "https://firebasestorage.googleapis.com/"
    const val UserProfileFragmentReason = "UserProfileFragmentReason"
    const val UserProfileFragmentSelectedUser = "UserProfileFragmentSelectedUser"
    const val ConversationDetailFragmentSelectedUser = "UserProfileFragmentSelectedUser"
    const val ConversationMediaFragmentUser = "ConversationMediaFragmentUser"
    const val ConversationMediaFragmentSelectedChat = "ConversationMediaFragmentSelectedMedia"

    const val FIREBASE_MESSAGING_TOKEN = "FIREBASE_MESSAGING_TOKEN"

    //    val randomImageUrl = "https://firebasestorage.googleapis.com/v0/b/voipapp-dbc05.appspot.com/o/USER_PROFILE_IMAGE%2Fw5VU6dSV4Jc56oiyb7LSH2vzwX92?alt=media&token=a0c50e33-2175-4d5e-badd-e7a58eac0446"
    const val UserListFragmentTag = "UserListFragment"
    const val UserProfileFragmentTag = "UserProfileFragmentTag"
    const val ConversationDetailFragmentTag = "ConversationDetailFragmentTag"
    const val MediaGalleryFragmentTag = "MediaGalleryFragmentTag"
    const val NearlyUserList = "NearlyUserList"
    const val SelectedUserUid = "SelectedUserUid"
    const val NotificationChannelID = "Soip App Notification Channel"
    const val VerificationId = "verificationId"


    const val REQUEST_FINE_LOCATION_PERMISSIONS_REQUEST_CODE = 34
    const val REQUEST_BACKGROUND_LOCATION_PERMISSIONS_REQUEST_CODE = 56

    const val MIN_PASSWORD_CHARACTER_COUNT = 6

    //User Fileds
    const val User = "USER"
    const val uid = "uid"
    const val name = "name"
    const val info = "info"
    const val profileImage = "profileImage"
    const val gender = "gender"
    const val location = "location"
    const val nat = "nat"


    //Location
    const val nearlyLimit: Double = 10.0//Kilometer
    const val userFetchStep: Int = 10

    //Database
    const val APP_DATABASE = "app-database"
    const val CHAT_TABLE = "chat_table"
    const val CONVERSATION_TABLE = "conversation_table"
    const val USER_TABLE = "user_table"


    //DB_CONSTANTS
    const val DESTINATION_UID = "destinationUid"
    const val MESSAGE_EVENT_TYPE = "messageEventType"
    const val CHAT_EVENT_TYPE = "chatEventType"
    const val CONTENT = "content"
    const val CHAT = "chat"
    const val UID = "uid"
    const val USER_UID = "userUid"
    const val DIRECTION = "direction"
    const val IS_SEEN = "is_seen"
    const val TYPE = "type"
    const val TEXT = "text"
    const val CONTENT_URL = "contentUrl"
    const val CREATE_DATE = "createDate"
    const val UPDATE_DATE = "updateDate"


    //Shared Keys
    const val enable_background_location_key = "enable_background_location_key"
    const val map_visibility_key = "map_visibility_key"
}