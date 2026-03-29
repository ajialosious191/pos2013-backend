package com.pos2013.offline.data.local 
 
import androidx.room.Dao 
import androidx.room.Insert 
import androidx.room.Query 
 
@Dao 
interface ErrorLogDao { 
 
    @Insert 
    suspend fun insert(log: ErrorLog) 
 
    @Query("SELECT * FROM error_logs ORDER BY timestamp DESC") 
    suspend fun getAll(): List<ErrorLog> 
 
    @Query("DELETE FROM error_logs") 
    suspend fun clearAll() 
} 
