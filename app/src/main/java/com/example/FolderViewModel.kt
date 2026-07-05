package com.example

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class FolderViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("FolderUntanglerPrefs", Context.MODE_PRIVATE)

    private val _targetPath = MutableStateFlow(
        sharedPrefs.getString("target_path", "/storage/emulated/0/Download") ?: "/storage/emulated/0/Download"
    )
    val targetPath: StateFlow<String> = _targetPath.asStateFlow()

    private val _isDryRun = MutableStateFlow(true)
    val isDryRun: StateFlow<Boolean> = _isDryRun.asStateFlow()

    private val _hasPermission = MutableStateFlow(false)
    val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _logs = MutableStateFlow<List<String>>(emptyList())
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    private val _actions = MutableStateFlow<List<UntangleAction>>(emptyList())
    val actions: StateFlow<List<UntangleAction>> = _actions.asStateFlow()

    private val _summary = MutableStateFlow<UntangleSummary?>(null)
    val summary: StateFlow<UntangleSummary?> = _summary.asStateFlow()

    private val _history = MutableStateFlow<List<String>>(
        sharedPrefs.getStringSet("folder_history", emptySet())?.toList()?.sorted() ?: emptyList()
    )
    val history: StateFlow<List<String>> = _history.asStateFlow()

    init {
        updatePermissionStatus()
    }

    fun updatePermissionStatus() {
        _hasPermission.value = checkHasPermission()
    }

    private fun checkHasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            val read = androidx.core.content.ContextCompat.checkSelfPermission(
                getApplication(),
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            val write = androidx.core.content.ContextCompat.checkSelfPermission(
                getApplication(),
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            read && write
        }
    }

    fun setTargetPath(path: String) {
        _targetPath.value = path
        sharedPrefs.edit().putString("target_path", path).apply()
        addToHistory(path)
    }

    fun toggleDryRun(value: Boolean) {
        _isDryRun.value = value
    }

    private fun addToHistory(path: String) {
        if (path.isBlank()) return
        val updatedSet = _history.value.toMutableSet()
        if (updatedSet.add(path)) {
            _history.value = updatedSet.toList().sorted()
            sharedPrefs.edit().putStringSet("folder_history", updatedSet).apply()
        }
    }

    fun removeHistoryItem(path: String) {
        val updatedSet = _history.value.toMutableSet()
        if (updatedSet.remove(path)) {
            _history.value = updatedSet.toList().sorted()
            sharedPrefs.edit().putStringSet("folder_history", updatedSet).apply()
        }
    }

    fun runUntangler() {
        if (_isRunning.value) return
        _isRunning.value = true

        viewModelScope.launch {
            val path = _targetPath.value
            val dryRun = _isDryRun.value

            _logs.value = listOf("Initializing untangling process on: $path...")
            _actions.value = emptyList()
            _summary.value = null

            val result = withContext(Dispatchers.IO) {
                UntanglerEngine.process(path, dryRun)
            }

            _logs.value = result.logs
            _actions.value = result.actions
            _summary.value = result
            _isRunning.value = false
        }
    }

    fun clearLogs() {
        _logs.value = emptyList()
        _actions.value = emptyList()
        _summary.value = null
    }
}
