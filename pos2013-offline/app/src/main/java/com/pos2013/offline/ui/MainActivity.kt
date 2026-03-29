package com.pos2013.offline.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pos2013.offline.R
import com.pos2013.offline.domain.sendTestBatch
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.newTransactionBtn).setOnClickListener {
            startActivity(Intent(this, NewTransactionActivity::class.java))
        }

        findViewById<Button>(R.id.historyBtn).setOnClickListener {
            startActivity(Intent(this, TransactionListActivity::class.java))
        }
        
        // Trigger the test batch on startup
        lifecycleScope.launch {
            val result = sendTestBatch(this@MainActivity)
            if (result.isSuccess) {
                Toast.makeText(this@MainActivity, "Test Batch Sent Successfully!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this@MainActivity, "Batch Failed: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
