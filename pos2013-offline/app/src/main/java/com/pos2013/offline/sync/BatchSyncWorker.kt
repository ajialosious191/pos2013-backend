package com.pos2013.offline.sync 
 
import android.content.Context 
import androidx.work.CoroutineWorker 
import androidx.work.WorkerParameters 
import com.pos2013.offline.data.local.DatabaseProvider 
import com.pos2013.offline.data.local.ErrorLog
import com.pos2013.offline.data.remote.ApiClient 
import com.pos2013.offline.data.remote.BatchRequestDto 
import com.pos2013.offline.data.remote.BatchTransactionDto 
import com.pos2013.offline.util.NotificationHelper
import java.util.UUID 
 
class BatchSyncWorker( 
    private val appContext: Context, 
    workerParams: WorkerParameters 
) : CoroutineWorker(appContext, workerParams) { 
 
    override suspend fun doWork(): Result { 
        return try { 
            val db = DatabaseProvider.get(appContext) 
            val dao = db.transactionDao() 
            val errorDao = db.errorLogDao()
 
            // 1. Get all pending offline transactions 
            val pending = dao.getPendingTransactions() 
            if (pending.isEmpty()) { 
                return Result.success() 
            } 
 
            // 2. Convert to batch DTO 
            val now = System.currentTimeMillis() 
            val batchId = UUID.randomUUID().toString() 
 
            val txDtos = pending.map { 
                BatchTransactionDto( 
                    localId = it.localId, 
                    code = it.code, 
                    amountMinor = it.amountMinor, 
                    currency = it.currency, 
                    createdAt = it.createdAt,
                    items = it.items,
                    subtotalMinor = it.subtotalMinor,
                    taxMinor = it.taxMinor,
                    discountMinor = it.discountMinor
                ) 
            } 
 
            val batch = BatchRequestDto( 
                merchantId = pending.first().merchantId, 
                terminalId = pending.first().terminalId, 
                batchId = batchId, 
                timestamp = now, 
                nonce = UUID.randomUUID().toString(), 
                transactions = txDtos 
            ) 
 
            // 3. Send to backend 
            val response = ApiClient.api(appContext).sendBatch(batch) 
 
            if (!response.isSuccessful || response.body()?.success != true) { 
                val errorMsg = "Backend rejected batch. Check Diagnostics."
                errorDao.insert( 
                    ErrorLog( 
                        timestamp = System.currentTimeMillis(), 
                        message = "Backend rejected batch", 
                        details = "Code: ${response.code()}, Body: ${response.errorBody()?.string()}" 
                    ) 
                ) 
                NotificationHelper.showSyncError(appContext, errorMsg)
                return Result.retry() 
            } 
 
            val results = response.body()?.results ?: emptyList() 
 
            // 4. Mark each transaction as synced 
            results.forEach { result -> 
                dao.markSynced( 
                    localId = result.localId, 
                    backendId = result.transactionId 
                ) 
            } 
 
            Result.success() 
 
        } catch (e: Exception) { 
            e.printStackTrace() 
            NotificationHelper.showSyncError(appContext, "Sync failed: ${e.message}")
            try {
                val db = DatabaseProvider.get(appContext)
                db.errorLogDao().insert( 
                    ErrorLog( 
                        timestamp = System.currentTimeMillis(), 
                        message = e.message ?: "Unknown sync error", 
                        details = e.stackTraceToString() 
                    ) 
                )
            } catch (inner: Exception) {
                inner.printStackTrace()
            }
            Result.retry() 
        } 
    } 
} 
