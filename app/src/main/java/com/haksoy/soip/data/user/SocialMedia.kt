package com.haksoy.soip.data.user

import java.io.Serializable

data class SocialMedia(
    var instagram: String? = null,
    var facebook: String? = null,
    var twitter: String? = null
) : Serializable {
    constructor() : this("", "", "")

    override fun toString(): String {
        return "$instagram:$facebook:$twitter"
    }

    companion object {
        fun toSocialMedia(input: String): SocialMedia {
            return SocialMedia(input.split(":")[0], input.split(":")[1], input.split(":")[2])
        }
    }
}