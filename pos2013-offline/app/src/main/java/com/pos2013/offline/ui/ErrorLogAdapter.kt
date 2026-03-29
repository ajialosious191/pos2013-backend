package com.pos2013.offline.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pos2013.offline.data.local.ErrorLog
import com.pos2013.offline.databinding.ItemErrorLogBinding
import java.text.SimpleDateFormat
import java.util.*

class ErrorLogAdapter(
    private var items: List<ErrorLog>
) : RecyclerView.Adapter<ErrorLogAdapter.LogViewHolder>() {

    inner class LogViewHolder(val binding: ItemErrorLogBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val binding = ItemErrorLogBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return LogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        val log = items[position]

        val sdf = SimpleDateFormat("dd MMM HH:mm:ss", Locale.getDefault())
        holder.binding.logTimestamp.text = sdf.format(Date(log.timestamp))
        holder.binding.logMessage.text = log.message
        holder.binding.logDetails.text = log.details ?: "No additional details"
    }

    override fun getItemCount() = items.size

    fun update(newItems: List<ErrorLog>) {
        items = newItems
        notifyDataSetChanged()
    }
}
