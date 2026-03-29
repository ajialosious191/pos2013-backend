package com.pos2013.offline.data.local 
 
import androidx.room.Entity 
import androidx.room.PrimaryKey 
 
@Entity(tableName = "error_logs") 
data class ErrorLog( 
    @PrimaryKey(autoGenerate = true) 
    val id: Long = 0, 
    val timestamp: Long, 
    val message: String, 
    val details: String? = null 
) 
