package com.godongijo.ecotainment.utilities

import android.util.Log
import com.godongijo.ecotainment.services.ApiService
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val BASE_URL = "https://admin.botanicabeautysalon.my.id/"

    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Log seluruh isi request dan response
        }
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request()
            val response = chain.proceed(request)
            val rawJson = response.body?.string()
            Log.d("API Response", "Raw JSON: $rawJson")
            response.newBuilder()
                .body(ResponseBody.create(response.body?.contentType(), rawJson ?: ""))
                .build()
        }
        .build()


    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
//            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}