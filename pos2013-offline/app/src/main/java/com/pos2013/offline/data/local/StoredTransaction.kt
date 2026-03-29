package com.pos2013.offline.data.local 
 
import androidx.room.Entity 
import androidx.room.PrimaryKey 
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class InvoiceItem(
    val name: String,
    val qty: Int,
    val price: Int // minor units
)

class InvoiceItemsConverter {
    @TypeConverter
    fun fromString(value: String): List<InvoiceItem> {
        val listType = object : TypeToken<List<InvoiceItem>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromList(list: List<InvoiceItem>): String {
        return Gson().toJson(list)
    }
}

@Entity(tableName = "transactions") 
data class StoredTransaction( 
    @PrimaryKey(autoGenerate = true) 
    val id: Long = 0, 
 
    val localId: String,          // UUID generated on device 
    val code: String,             // 6-digit code 
    val amountMinor: Long,        // amount in minor units 
    val currency: String = "AED", 
 
    val merchantId: String, 
    val terminalId: String, 
 
    val items: List<InvoiceItem> = emptyList(),
    val subtotalMinor: Long = 0,
    val taxMinor: Long = 0,
    val discountMinor: Long = 0,

    val createdAt: Long,          // timestamp 
    val synced: Boolean = false,  // false = pending, true = synced 
    val backendTransactionId: String? = null // returned by backend 
) 
