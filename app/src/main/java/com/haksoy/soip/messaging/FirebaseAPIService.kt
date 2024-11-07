package com.haksoy.soip.messaging

import com.haksoy.soip.data.message.MessageBody
import com.haksoy.soip.data.message.MessageResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface FirebaseAPIService {

    @Headers(
        "Content-Type:application/json",
        "Authorization:Bearer BHIkZyyYYHT_SxO_z0kcnzkMdCmcGLo3kYe1Yi5DSDTUNugPzQwTyZ8bWcH5IMr9fYNwgRMPGkaPScoFwnYmEW8"

    )
    @POST("v1/projects/soip-95fce/messages:send")
    fun sendNotification(@Body messageBody: MessageBody): Call<MessageResponse>

}