package com.example.net

import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface ApiService {

    @POST("activate")
    suspend fun activateRaw(
        @Header("ngrok-skip-browser-warning") skipWarning: String = "true",
        @Body pin: RequestBody
    ): Response<ResponseBody>

    @POST("activate")
    @FormUrlEncoded
    suspend fun activateForm(
        @Header("ngrok-skip-browser-warning") skipWarning: String = "true",
        @Field("pin") pin: String
    ): Response<ResponseBody>

    @POST("activate")
    suspend fun activateMap(
        @Header("ngrok-skip-browser-warning") skipWarning: String = "true",
        @Body body: Map<String, String>
    ): Response<ResponseBody>

    @POST("open")
    @FormUrlEncoded
    suspend fun openBarrier(
        @Header("ngrok-skip-browser-warning") skipWarning: String = "true",
        @Field("hash") hash: String,
        @Field("lat") lat: Double,
        @Field("lon") lon: Double
    ): Response<ResponseBody>

    @POST("open")
    suspend fun openBarrierJson(
        @Header("ngrok-skip-browser-warning") skipWarning: String = "true",
        @Body body: Map<String, String>
    ): Response<ResponseBody>

    @GET("/")
    suspend fun heartbeat(
        @Header("ngrok-skip-browser-warning") skipWarning: String = "true"
    ): Response<ResponseBody>
}

object RetrofitClient {
    private const val BASE_URL = "https://ambiguity-crumpled-blinks.ngrok-free.dev/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
