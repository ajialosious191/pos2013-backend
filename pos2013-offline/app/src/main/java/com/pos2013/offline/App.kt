package com.pos2013.offline 
 
import android.app.Application 
import androidx.work.ExistingPeriodicWorkPolicy 
import androidx.work.PeriodicWorkRequestBuilder 
import androidx.work.WorkManager 
import com.pos2013.offline.sync.BatchSyncWorker 
import com.pos2013.offline.util.NotificationHelper
import java.util.concurrent.TimeUnit 
 
class App : Application() { 
 
    override fun onCreate() { 
        super.onCreate() 
 
        // Initialize Notification Channels
        NotificationHelper.createChannels(this)

        // Run sync every 15 minutes (minimum allowed by Android) 
        val syncRequest = PeriodicWorkRequestBuilder<BatchSyncWorker>( 
            15, TimeUnit.MINUTES 
        ).build() 
 
        WorkManager.getInstance(this).enqueueUniquePeriodicWork( 
            "batch_sync_worker", 
            ExistingPeriodicWorkPolicy.KEEP, 
            syncRequest 
        ) 
    } 
} 
