package com.pos2013.offline.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.pos2013.offline.data.local.DatabaseProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = DatabaseProvider.get(app).transactionDao()

    private val _pendingCount = MutableLiveData<Int>()
    val pendingCount: LiveData<Int> = _pendingCount

    init {
        startSyncStatusRefresh()
    }

    private fun startSyncStatusRefresh() {
        viewModelScope.launch {
            while (true) {
                _pendingCount.value = dao.getPendingCount()
                delay(5000) // Refresh every 5 seconds
            }
        }
    }

    fun refreshPendingCount() {
        viewModelScope.launch {
            _pendingCount.value = dao.getPendingCount()
        }
    }

    fun refreshStatus() {
        refreshPendingCount()
    }
}
