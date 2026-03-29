package com.pos2013.offline.ui 
 
import android.app.Activity 
import android.content.Context
import android.os.Bundle 
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.journeyapps.barcodescanner.ScanContract 
import com.journeyapps.barcodescanner.ScanOptions 
import org.json.JSONObject

class PairingActivity : ComponentActivity() { 
 
    private val scanner = registerForActivityResult(ScanContract()) { result -> 
        if (result.contents != null) { 
            try {
                val json = result.contents 
                val obj = JSONObject(json) 
 
                val merchantId = obj.getString("merchantId") 
                val terminalId = obj.getString("terminalId") 
                val apiKey = obj.getString("apiKey") 
 
                val branding = obj.optJSONObject("branding")
                val merchantName = branding?.optString("name", "Merchant") ?: "Merchant"
                val logoUrl = branding?.optString("logo_url", "") ?: ""
                val brandColor = branding?.optString("brand_color", "#1976d2") ?: "#1976d2"

                val prefs = getSharedPreferences("pos2013", Context.MODE_PRIVATE) 
                prefs.edit() 
                    .putString("merchantId", merchantId) 
                    .putString("terminalId", terminalId) 
                    .putString("apiKey", apiKey) 
                    .putString("merchantName", merchantName)
                    .putString("logoUrl", logoUrl)
                    .putString("brandColor", brandColor)
                    .apply() 
 
                setResult(Activity.RESULT_OK) 
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } 
        finish() 
    } 
 
    override fun onCreate(savedInstanceState: Bundle?) { 
        super.onCreate(savedInstanceState) 
 
        val options = ScanOptions() 
        options.setPrompt("Scan Terminal Pairing QR") 
        options.setBeepEnabled(true) 
        options.setOrientationLocked(true) 
 
        scanner.launch(options) 
    } 
} 
