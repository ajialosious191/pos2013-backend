package com.pos2013.offline.ui 
 
import android.app.Application 
import androidx.lifecycle.AndroidViewModel 
import androidx.lifecycle.MutableLiveData 
import androidx.lifecycle.viewModelScope 
import com.pos2013.offline.data.local.DatabaseProvider 
import com.pos2013.offline.data.local.ErrorLog 
import kotlinx.coroutines.launch 
 
class DiagnosticsViewModel(app: Application) : AndroidViewModel(app) { 
 
    private val dao = DatabaseProvider.get(app).errorLogDao() 
 
    val logs = MutableLiveData<List<ErrorLog>>() 
 
    fun loadLogs() { 
        viewModelScope.launch { 
            logs.value = dao.getAll() 
        } 
    } 
 
    fun clearLogs(onDone: () -> Unit) { 
        viewModelScope.launch { 
            dao.clearAll() 
            onDone() 
            loadLogs() 
        } 
    } 
} 
