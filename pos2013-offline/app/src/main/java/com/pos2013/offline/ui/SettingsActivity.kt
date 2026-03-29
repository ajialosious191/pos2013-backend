package com.pos2013.offline.ui 
 
import android.content.Context
import android.content.Intent 
import android.os.Bundle 
import android.widget.Toast 
import androidx.activity.ComponentActivity 
import com.pos2013.offline.databinding.ActivitySettingsBinding 
 
class SettingsActivity : ComponentActivity() { 
 
    private lateinit var binding: ActivitySettingsBinding 
 
    override fun onCreate(savedInstanceState: Bundle?) { 
        super.onCreate(savedInstanceState) 
        binding = ActivitySettingsBinding.inflate(layoutInflater) 
        setContentView(binding.root) 
 
        loadCredentials() 
 
        binding.scanQrBtn.setOnClickListener { 
            val intent = Intent(this, PairingActivity::class.java) 
            startActivity(intent) 
        } 
 
        binding.clearBtn.setOnClickListener { 
            val prefs = getSharedPreferences("pos2013", Context.MODE_PRIVATE) 
            prefs.edit().clear().apply() 
            Toast.makeText(this, "Credentials cleared", Toast.LENGTH_SHORT).show() 
            loadCredentials() 
        } 
    } 
 
    private fun loadCredentials() { 
        val prefs = getSharedPreferences("pos2013", Context.MODE_PRIVATE) 
 
        val merchantId = prefs.getString("merchantId", "-") 
        val terminalId = prefs.getString("terminalId", "-") 
        val apiKey = prefs.getString("apiKey", null) 
 
        binding.merchantIdText.text = merchantId 
        binding.terminalIdText.text = terminalId 
 
        binding.apiKeyText.text = if (apiKey == null) { 
            "Not Paired" 
        } else { 
            "************" // masked 
        } 
    }

    override fun onResume() {
        super.onResume()
        loadCredentials()
    }
} 
