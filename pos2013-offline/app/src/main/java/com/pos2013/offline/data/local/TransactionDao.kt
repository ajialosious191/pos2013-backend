package com.pos2013.offline.data.local 
 
import androidx.room.Dao 
import androidx.room.Insert 
import androidx.room.Query 
import androidx.room.Update 
 
@Dao 
interface TransactionDao { 
 
    @Insert 
    suspend fun insert(tx: StoredTransaction) 
 
    @Query("SELECT * FROM transactions WHERE synced = 0") 
    suspend fun getPendingTransactions(): List<StoredTransaction> 
 
    @Update 
    suspend fun update(tx: StoredTransaction) 
 
    @Query("UPDATE transactions SET synced = 1, backendTransactionId = :backendId WHERE localId = :localId") 
    suspend fun markSynced(localId: String, backendId: String) 

    @Query("SELECT * FROM transactions ORDER BY createdAt DESC") 
    suspend fun getAllTransactions(): List<StoredTransaction> 

    @Query("SELECT COUNT(*) FROM transactions WHERE synced = 0")
    suspend fun getPendingCount(): Int
} 
