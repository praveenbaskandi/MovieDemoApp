package com.example.moviedemoapp.network

import com.example.moviedemoapp.BuildConfig
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

object NetworkModule {
    private const val BASE_URL = "https://api.themoviedb.org/3/"
    private val json = Json { ignoreUnknownKeys = true }

    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val url = originalRequest.url.newBuilder()
            .build()
        
        val newRequest = originalRequest.newBuilder()
            .url(url)
            .addHeader(
                "Authorization",
                "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI3MWEzNmE0ZWU3M2JlZDIyM2ViNDM4YjllYWJiNzNjYSIsIm5iZiI6MTc1MDkxNTg3NC43MjYsInN1YiI6IjY4NWNkYjIyMzVmMzE3YzQ1MjQ3MWEwYSIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.qhS8CPbKMAcjpPRipKjeFP2qa9VZvm6r92nXqFJ5w2E"
            )
            .addHeader("Accept", "application/json")
            .build()
        chain.proceed(newRequest)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val tmdbApiService: TmdbApiService = retrofit.create(TmdbApiService::class.java)
}
