package com.pos2013.offline.data.remote 

import android.content.Context
import okhttp3.OkHttpClient
import retrofit2.Retrofit 
import retrofit2.converter.gson.GsonConverterFactory 

object ApiClient { 

    // For local testing on emulator use: "http://10.0.2.2:10000"
    // For local testing on physical device use: "http://YOUR_LOCAL_IP:10000"
    private const val BASE_URL = "https://pos2013-backend.onrender.com" 

    fun api(context: Context): Pos2013Api { 
        val prefs = context.getSharedPreferences("pos2013", Context.MODE_PRIVATE) 
        val terminalId = prefs.getString("terminalId", "") ?: ""
        val apiKey = prefs.getString("apiKey", "") ?: ""

        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val request = original.newBuilder()
                    .header("X-Terminal-Id", terminalId)
                    .header("X-Api-Key", apiKey)
                    .method(original.method, original.body)
                    .build()
                chain.proceed(request)
            }
            .build()

        return Retrofit.Builder() 
            .baseUrl(BASE_URL) 
            .client(client)
            .addConverterFactory(GsonConverterFactory.create()) 
            .build() 
            .create(Pos2013Api::class.java) 
    } 
} 
