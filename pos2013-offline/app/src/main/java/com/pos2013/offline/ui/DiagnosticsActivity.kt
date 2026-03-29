package com.pos2013.offline.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.pos2013.offline.databinding.ActivityDiagnosticsBinding

class DiagnosticsActivity : ComponentActivity() {

    private lateinit var binding: ActivityDiagnosticsBinding
    private val viewModel: DiagnosticsViewModel by viewModels()
    private lateinit var adapter: ErrorLogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiagnosticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ErrorLogAdapter(emptyList())
        binding.logRecycler.layoutManager = LinearLayoutManager(this)
        binding.logRecycler.adapter = adapter

        viewModel.logs.observe(this) {
            adapter.update(it)
        }

        binding.clearBtn.setOnClickListener {
            viewModel.clearLogs {
                Toast.makeText(this, "Logs cleared", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.loadLogs()
    }
}
