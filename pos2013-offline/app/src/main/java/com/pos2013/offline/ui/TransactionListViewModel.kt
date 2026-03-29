package com.pos2013.offline.ui 
 
import android.app.Application 
import androidx.lifecycle.AndroidViewModel 
import androidx.lifecycle.LiveData 
import androidx.lifecycle.MutableLiveData 
import androidx.lifecycle.viewModelScope 
import com.pos2013.offline.data.local.DatabaseProvider 
import com.pos2013.offline.data.local.StoredTransaction 
import kotlinx.coroutines.launch 
 
class TransactionListViewModel(app: Application) : AndroidViewModel(app) { 
 
    private val dao = DatabaseProvider.get(app).transactionDao() 
 
    private val _transactions = MutableLiveData<List<StoredTransaction>>() 
    val transactions: LiveData<List<StoredTransaction>> = _transactions 
 
    fun loadTransactions() { 
        viewModelScope.launch { 
            _transactions.value = dao.getAllTransactions() 
        } 
    } 
} 
