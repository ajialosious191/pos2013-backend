package com.pos2013.offline.domain 

import android.content.Context
import com.pos2013.offline.data.remote.ApiClient 
import com.pos2013.offline.data.remote.BatchRequestDto 
import com.pos2013.offline.data.remote.BatchTransactionDto 
import kotlinx.coroutines.Dispatchers 
import kotlinx.coroutines.withContext 
import java.util.UUID 
 
suspend fun sendTestBatch(context: Context): Result<Unit> = withContext(Dispatchers.IO) { 
    try { 
        val now = System.currentTimeMillis() 
 
        val tx = BatchTransactionDto( 
            localId = "local-android-001", 
            code = "123456", 
            amountMinor = 20130, 
            currency = "AED", 
            createdAt = now - 60_000 
        ) 
 
        val batch = BatchRequestDto( 
            merchantId = "AL_RKN_AL_RAQY", 
            terminalId = "T001", 
            batchId = UUID.randomUUID().toString(), 
            timestamp = now, 
            nonce = UUID.randomUUID().toString(), 
            transactions = listOf(tx) 
        ) 
 
        val response = ApiClient.api(context).sendBatch(batch) 

        if (response.isSuccessful && response.body()?.success == true) { 
            Result.success(Unit) 
        } else { 
            Result.failure(Exception("Batch failed: ${response.code()} ${response.errorBody()?.string()}")) 
        } 
    } catch (e: Exception) { 
        Result.failure(e) 
    } 
} 
