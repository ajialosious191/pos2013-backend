package com.pos2013.offline.data.remote 

import retrofit2.Response 
import retrofit2.http.Body 
import retrofit2.http.Header 
import retrofit2.http.POST 

interface Pos2013Api { 

    @POST("/api/2013/batch") 
    suspend fun sendBatch( 
        @Body batch: BatchRequestDto 
    ): Response<BatchResponseDto> 
} 
