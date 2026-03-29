package com.pos2013.offline.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.pos2013.offline.data.local.DatabaseProvider
import com.pos2013.offline.databinding.ActivityDailySummaryBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DailySummaryActivity : ComponentActivity() {

    private lateinit var binding: ActivityDailySummaryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDailySummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadSummary()

        binding.refreshBtn.setOnClickListener {
            loadSummary()
        }
    }

    private fun loadSummary() {
        val dao = DatabaseProvider.get(this).transactionDao()

        lifecycleScope.launch {
            val all = dao.getAllTransactions()
            
            // Filter for today
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.timeInMillis
            
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            val endOfDay = calendar.timeInMillis

            val todayTx = all.filter { it.createdAt in startOfDay..endOfDay }

            val totalCount = todayTx.size
            val pendingCount = todayTx.count { !it.synced }

            // Group totals by currency
            val totalsByCurrency = todayTx.groupBy { it.currency }
                .mapValues { (_, txs) -> txs.sumOf { it.amountMinor } }

            val summaryText = if (totalsByCurrency.isEmpty()) {
                "0.00"
            } else {
                totalsByCurrency.entries.joinToString("\n") { (curr, amount) ->
                    "$curr %.2f".format(amount / 100.0)
                }
            }

            binding.totalAmount.text = summaryText
            binding.totalCount.text = totalCount.toString()
            binding.pendingCount.text = pendingCount.toString()
            
            // Optional: Change pending color if > 0
            if (pendingCount > 0) {
                binding.pendingCount.setTextColor(android.graphics.Color.parseColor("#E53E3E"))
            } else {
                binding.pendingCount.setTextColor(android.graphics.Color.parseColor("#38A169"))
            }
        }
    }
}
