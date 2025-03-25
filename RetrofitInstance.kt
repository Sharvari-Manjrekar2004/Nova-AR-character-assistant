package com.xperiencelabs.astronaut.screens

import GeminiApiService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object GeminiRetrofitInstance {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"
    private const val API_KEY = "AIzaSyC930rmU5zGtYo_Qahl-bU2P5FALz9iDgw" // Replace with your actual API key

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val original = chain.request()
            val url = original.url.newBuilder()
                .addQueryParameter("key", API_KEY) // Add API key as query parameter
                .build()
            val request = original.newBuilder().url(url).build()
            chain.proceed(request)
        }
        .build()

    val api: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client) // Attach the custom client with interceptor
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiApiService::class.java)
    }
}
