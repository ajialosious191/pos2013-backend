package com.pos2013.offline.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.pos2013.offline.R
import com.pos2013.offline.databinding.ActivityHomeBinding
import com.pos2013.offline.sync.BatchSyncWorker

class HomeActivity : ComponentActivity() {

    private lateinit var binding: ActivityHomeBinding
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dot = binding.syncStatusDot

        // Observe pending transactions count
        viewModel.pendingCount.observe(this) { pending ->
            binding.pendingCountText.text = "Pending: $pending"
            binding.syncStatusText.text = if (pending == 0) "Sync Status: Up to date" else "Sync Status: Pending sync"

            // Smooth fade animation for the status dot
            dot.animate().alpha(0f).setDuration(150).withEndAction {
                dot.setBackgroundResource(
                    if (pending == 0) R.drawable.status_dot_green
                    else R.drawable.status_dot_red
                )
                dot.animate().alpha(1f).setDuration(150).start()
            }.start()
        }

        // Navigation buttons
        binding.newTransactionBtn.setOnClickListener {
            startActivity(Intent(this, NewTransactionActivity::class.java))
        }

        binding.historyBtn.setOnClickListener {
            startActivity(Intent(this, TransactionListActivity::class.java))
        }

        binding.summaryBtn.setOnClickListener {
            startActivity(Intent(this, DailySummaryActivity::class.java))
        }

        binding.pairTerminalBtn.setOnClickListener {
            startActivity(Intent(this, PairingActivity::class.java))
        }

        binding.settingsBtn.setOnClickListener { 
            startActivity(Intent(this, SettingsActivity::class.java)) 
        } 

        binding.syncNowBtn.setOnClickListener { 
            triggerManualSync() 
        }

        binding.diagnosticsBtn.setOnClickListener {
            startActivity(Intent(this, DiagnosticsActivity::class.java))
        }

        observeManualSync()
    }

    private fun triggerManualSync() { 
        val request = OneTimeWorkRequestBuilder<BatchSyncWorker>().build() 
 
        WorkManager.getInstance(this).enqueueUniqueWork( 
            "manual_sync", 
            ExistingWorkPolicy.REPLACE, 
            request 
        ) 
 
        binding.syncStatusText.text = "Sync Status: Syncing..." 
        binding.syncStatusDot.setBackgroundResource(R.drawable.status_dot_red)
        binding.syncNowBtn.isEnabled = false 
        binding.syncNowBtn.text = "Syncing..." 
    }

    private fun observeManualSync() {
        WorkManager.getInstance(this) 
            .getWorkInfosForUniqueWorkLiveData("manual_sync") 
            .observe(this) { infos -> 
                if (infos.isNullOrEmpty()) return@observe 
 
                val info = infos[0] 
 
                when (info.state) { 
                    WorkInfo.State.SUCCEEDED -> { 
                        binding.syncStatusText.text = "Sync Status: All Synced" 
                        binding.syncStatusDot.setBackgroundResource(R.drawable.status_dot_green) 
                        binding.syncNowBtn.isEnabled = true 
                        binding.syncNowBtn.text = "Sync Now" 
                        viewModel.refreshStatus() 
                        Toast.makeText(this, "Sync completed", Toast.LENGTH_SHORT).show()
                    } 
 
                    WorkInfo.State.FAILED, 
                    WorkInfo.State.CANCELLED -> { 
                        binding.syncStatusText.text = "Sync Status: Failed" 
                        binding.syncStatusDot.setBackgroundResource(R.drawable.status_dot_red) 
                        binding.syncNowBtn.isEnabled = true 
                        binding.syncNowBtn.text = "Sync Now" 
                        Toast.makeText(this, "Sync failed", Toast.LENGTH_SHORT).show()
                    } 
 
                    else -> { /* ignore */ } 
                } 
            }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPendingCount()
    }
}
