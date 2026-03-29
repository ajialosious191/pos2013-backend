package com.pos2013.offline.ui 
 
import android.app.Application 
import androidx.lifecycle.AndroidViewModel 
import androidx.lifecycle.viewModelScope 
import com.pos2013.offline.data.local.DatabaseProvider 
import com.pos2013.offline.data.local.StoredTransaction 
import kotlinx.coroutines.launch 
import com.pos2013.offline.data.local.InvoiceItem
import java.util.UUID

class TransactionViewModel(application: Application) : AndroidViewModel(application) {

    fun saveTransaction(
        code: String, 
        amount: Long, 
        currency: String, 
        merchantId: String, 
        terminalId: String,
        items: List<InvoiceItem> = emptyList(),
        subtotal: Long = 0,
        tax: Long = 0,
        discount: Long = 0,
        onDone: () -> Unit
    ) { 
        viewModelScope.launch { 
            val tx = StoredTransaction( 
                localId = UUID.randomUUID().toString(), 
                code = code, 
                amountMinor = amount, 
                currency = currency, 
                merchantId = merchantId, 
                terminalId = terminalId,
                items = items,
                subtotalMinor = subtotal,
                taxMinor = tax,
                discountMinor = discount,
                createdAt = System.currentTimeMillis() 
            ) 
 
            dao.insert(tx) 
            onDone() 
        } 
    } 
} 
