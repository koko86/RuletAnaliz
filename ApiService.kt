package com.example.ruletanaliz

import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/spin")
    suspend fun predictSpin(@Body request: SpinRequest): SpinResponse
}
