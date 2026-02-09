package com.novachat.core.network

import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Shared Json configuration for network serialization.
 */
object NetworkJson {
    val instance: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
        isLenient = true
    }
}

/**
 * Factory for creating OkHttpClient instances with optional interceptors.
 */
object NetworkClientFactory {
    fun createOkHttpClient(
        interceptors: List<Interceptor> = emptyList(),
        enableLogging: Boolean = false
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()

        if (enableLogging) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            builder.addInterceptor(logging)
        }

        interceptors.forEach { interceptor ->
            builder.addInterceptor(interceptor)
        }

        return builder.build()
    }
}

/**
 * Factory for creating Retrofit instances using kotlinx.serialization.
 */
object RetrofitFactory {
    fun createRetrofit(
        baseUrl: String,
        okHttpClient: OkHttpClient = NetworkClientFactory.createOkHttpClient(),
        json: Json = NetworkJson.instance
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }
}
