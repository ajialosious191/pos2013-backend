package com.pos2013.offline.ui 
 
import android.view.LayoutInflater 
import android.view.ViewGroup 
import androidx.recyclerview.widget.RecyclerView 
import com.pos2013.offline.data.local.StoredTransaction 
import com.pos2013.offline.databinding.ItemTransactionBinding 
import java.text.SimpleDateFormat 
import java.util.* 
 
class TransactionAdapter( 
    private var items: List<StoredTransaction> 
) : RecyclerView.Adapter<TransactionAdapter.TxViewHolder>() { 
 
    inner class TxViewHolder(val binding: ItemTransactionBinding) : 
        RecyclerView.ViewHolder(binding.root) 
 
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TxViewHolder { 
        val binding = ItemTransactionBinding.inflate( 
            LayoutInflater.from(parent.context), 
            parent, 
            false 
        ) 
        return TxViewHolder(binding) 
    } 
 
    override fun onBindViewHolder(holder: TxViewHolder, position: Int) { 
        val tx = items[position] 
 
        holder.binding.codeText.text = tx.code 
        val itemCount = if (tx.items.isNotEmpty()) " (${tx.items.size} items)" else ""
        holder.binding.amountText.text = "${tx.currency} %.2f$itemCount".format(tx.amountMinor / 100.0) 
 
        val date = Date(tx.createdAt) 
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) 
        holder.binding.dateText.text = sdf.format(date) 
 
        holder.binding.statusText.text = 
            if (tx.synced) "Synced" else "Pending" 
        
        // Optional: color status
        val statusColor = if (tx.synced) 0xFF2E7D32.toInt() else 0xFFD32F2F.toInt()
        holder.binding.statusText.setTextColor(statusColor)
    } 
 
    override fun getItemCount() = items.size 
 
    fun update(newItems: List<StoredTransaction>) { 
        items = newItems 
        notifyDataSetChanged() 
    } 
} 
