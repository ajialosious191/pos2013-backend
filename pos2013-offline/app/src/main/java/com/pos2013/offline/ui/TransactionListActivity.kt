package com.pos2013.offline.ui 
 
import android.os.Bundle 
import androidx.activity.ComponentActivity 
import androidx.activity.viewModels 
import androidx.recyclerview.widget.LinearLayoutManager 
import com.pos2013.offline.databinding.ActivityTransactionListBinding 
 
class TransactionListActivity : ComponentActivity() { 
 
    private lateinit var binding: ActivityTransactionListBinding 
    private val viewModel: TransactionListViewModel by viewModels() 
    private lateinit var adapter: TransactionAdapter 
 
    override fun onCreate(savedInstanceState: Bundle?) { 
        super.onCreate(savedInstanceState) 
        binding = ActivityTransactionListBinding.inflate(layoutInflater) 
        setContentView(binding.root) 
 
        adapter = TransactionAdapter(emptyList()) 
        binding.recyclerView.layoutManager = LinearLayoutManager(this) 
        binding.recyclerView.adapter = adapter 
 
        viewModel.transactions.observe(this) { 
            adapter.update(it) 
        } 
 
        viewModel.loadTransactions() 
    } 
} 
