package com.pos2013.offline.data.local 
 
import androidx.room.Database 
import androidx.room.RoomDatabase 
import androidx.room.TypeConverters

@Database( 
    entities = [StoredTransaction::class, ErrorLog::class], 
    version = 3, 
    exportSchema = false 
) 
@TypeConverters(InvoiceItemsConverter::class)
abstract class AppDatabase : RoomDatabase() { 
    abstract fun transactionDao(): TransactionDao 
    abstract fun errorLogDao(): ErrorLogDao
} 
