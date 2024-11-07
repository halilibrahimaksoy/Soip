package com.haksoy.soip.data.message

data class MessageResponse(
    val success: Int,
    val failure: Int,
    val canonical_ids: Int
)