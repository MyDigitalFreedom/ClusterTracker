package com.clustertracker.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clustertracker.app.data.datastore.AppPreferences
import com.clustertracker.app.data.datastore.PreferencesManager
import com.clustertracker.app.data.remote.GeocodingApiService
import com.clustertracker.app.data.remote.GeocodingResult
import com.clustertracker.app.util.DataExporter
import com.clustertracker.app.util.DataImporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val geocodingApi: GeocodingApiService,
    private val dataExporter: DataExporter,
    private val dataImporter: DataImporter
) : ViewModel() {

    val preferences: StateFlow<AppPreferences> = preferencesManager.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppPreferences())

    private val _citySearchResults = MutableStateFlow<List<GeocodingResult>>(emptyList())
    val citySearchResults: StateFlow<List<GeocodingResult>> = _citySearchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()

    fun updateApiKey(key: String) {
        viewModelScope.launch { preferencesManager.setWeatherApiKey(key) }
    }

    private var lastSearchTime = 0L

    fun searchCity(query: String) {
        if (query.length < 2) return
        val now = System.currentTimeMillis()
        if (now - lastSearchTime < 2000L) return // 2-second cooldown
        lastSearchTime = now

        val apiKey = preferences.value.weatherApiKey
        if (apiKey.isBlank()) {
            viewModelScope.launch {
                _events.emit(SettingsEvent.Error("Set your API key first"))
            }
            return
        }

        viewModelScope.launch {
            _isSearching.value = true
            try {
                val results = geocodingApi.searchCity(query = query, apiKey = apiKey)
                _citySearchResults.value = results
            } catch (e: Exception) {
                _events.emit(SettingsEvent.Error("Search failed: ${e.message}"))
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun selectCity(result: GeocodingResult) {
        viewModelScope.launch {
            preferencesManager.setCity(result.displayName, result.lat, result.lon)
            _citySearchResults.value = emptyList()
        }
    }

    fun setDarkTheme(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setDarkTheme(enabled) }
    }

    fun exportJson(): String = dataExporter.exportToJson()

    fun exportCsv(): Map<String, String> = dataExporter.exportToCsv()

    fun importJson(jsonString: String) {
        viewModelScope.launch {
            try {
                dataImporter.importFromJson(jsonString)
                _events.emit(SettingsEvent.ImportSuccess)
            } catch (e: Exception) {
                _events.emit(SettingsEvent.Error("Import failed: ${e.message}"))
            }
        }
    }

    sealed class SettingsEvent {
        data class Error(val message: String) : SettingsEvent()
        data object ImportSuccess : SettingsEvent()
    }
}
