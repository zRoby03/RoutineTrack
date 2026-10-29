package com.example.routinetrack.data.remote

import com.example.routinetrack.data.remote.dto.AuthResponseDto
import com.example.routinetrack.data.remote.dto.LoginRequestDto
import com.example.routinetrack.data.remote.dto.RegisterRequestDto
import com.example.routinetrack.data.remote.dto.SyncRequestDto
import com.example.routinetrack.data.remote.dto.SyncResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @GET("health")
    suspend fun health(): Map<String, String>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequestDto): AuthResponseDto

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): AuthResponseDto

    @GET("sync/{userId}")
    suspend fun getSyncData(
        @Header("Authorization") authorization: String,
        @Path("userId") userId: String
    ): SyncResponseDto

    @POST("sync/{userId}")
    suspend fun syncData(
        @Header("Authorization") authorization: String,
        @Path("userId") userId: String,
        @Body request: SyncRequestDto
    ): SyncResponseDto
}
