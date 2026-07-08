package com.example.routinetrack.data.remote

import com.example.routinetrack.data.remote.dto.AuthResponseDto
import com.example.routinetrack.data.remote.dto.CompletionDto
import com.example.routinetrack.data.remote.dto.HabitDto
import com.example.routinetrack.data.remote.dto.LoginRequestDto
import com.example.routinetrack.data.remote.dto.MessageResponseDto
import com.example.routinetrack.data.remote.dto.PasswordResetConfirmDto
import com.example.routinetrack.data.remote.dto.PasswordResetRequestDto
import com.example.routinetrack.data.remote.dto.RegisterRequestDto
import com.example.routinetrack.data.remote.dto.SyncRequestDto
import com.example.routinetrack.data.remote.dto.SyncResponseDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("health")
    suspend fun health(): Map<String, String>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequestDto): AuthResponseDto

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): AuthResponseDto

    @POST("auth/request-password-reset")
    suspend fun requestPasswordReset(@Body request: PasswordResetRequestDto): MessageResponseDto

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: PasswordResetConfirmDto): MessageResponseDto

    @GET("habits")
    suspend fun getHabits(@Query("userId") userId: String): List<HabitDto>

    @POST("habits")
    suspend fun createHabit(@Body habit: HabitDto): HabitDto

    @PUT("habits/{id}")
    suspend fun updateHabit(@Path("id") id: String, @Body habit: HabitDto): HabitDto

    @DELETE("habits/{id}")
    suspend fun deleteHabit(@Path("id") id: String)

    @GET("completions")
    suspend fun getCompletions(@Query("userId") userId: String): List<CompletionDto>

    @POST("completions")
    suspend fun createCompletion(@Body completion: CompletionDto): CompletionDto

    @PUT("completions/{id}")
    suspend fun updateCompletion(
        @Path("id") id: String,
        @Body completion: CompletionDto
    ): CompletionDto

    @GET("sync/{userId}")
    suspend fun getSyncData(@Path("userId") userId: String): SyncResponseDto

    @POST("sync/{userId}")
    suspend fun syncData(
        @Path("userId") userId: String,
        @Body request: SyncRequestDto
    ): SyncResponseDto
}
