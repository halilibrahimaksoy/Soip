package com.haksoy.soip.data.message

data class MessageBody(
    val to: String,
    val data: MessageData,
    val android: PriorityAndroid? = PriorityAndroid("normal"),
    val apns: PriorityIOS? = PriorityIOS(mapOf(Pair("apns-priority", "5")))
)