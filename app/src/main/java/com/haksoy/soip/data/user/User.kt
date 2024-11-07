package com.haksoy.soip.data.user

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.haksoy.soip.utlis.Constants
import java.io.Serializable
import java.util.*

@Entity(tableName = Constants.USER_TABLE)
data class User(
    @PrimaryKey var uid: String,
    var phoneNumber: String? = null,
    var name: String? = null,
    var info: String? = null,
    var profileImage: String? = null,
    var gender: String? = null,
    var location: Location = Location(),
    var nat: String? = null,
    var socialMedia: SocialMedia = SocialMedia(),
    var token: String? = null,
    var createDate: Long? = null,
    var registerData: Long? = null,
    var updatedDate: Long? = null
) : Serializable {
    constructor() : this(
        UUID.randomUUID().toString(),
        "",
        "",
        "",
        "",
        "",
        Location(),
        "",
        SocialMedia(),
        "", 0, 0, 0
    )
}