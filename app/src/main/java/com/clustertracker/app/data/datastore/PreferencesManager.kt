package com.clustertracker.app.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

private const val DEFAULT_WEATHER_API_KEY = "71485c56f0346f27cdb4916064d238c1"

data class AppPreferences(
    val weatherApiKey: String = DEFAULT_WEATHER_API_KEY,
    val cityName: String? = null,
    val cityLat: Double? = null,
    val cityLon: Double? = null,
    val defaultFlowRate: String = "15",
    val painLogDebounceMs: Long = 2000L,
    val darkTheme: Boolean = true
)

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    val preferencesFlow: Flow<AppPreferences> = dataStore.data.map { prefs ->
        AppPreferences(
            weatherApiKey = prefs[KEY_WEATHER_API_KEY] ?: DEFAULT_WEATHER_API_KEY,
            cityName = prefs[KEY_CITY_NAME],
            cityLat = prefs[KEY_CITY_LAT],
            cityLon = prefs[KEY_CITY_LON],
            defaultFlowRate = prefs[KEY_DEFAULT_FLOW_RATE] ?: "15",
            darkTheme = prefs[KEY_DARK_THEME] != "false"
        )
    }

    suspend fun setWeatherApiKey(key: String) {
        dataStore.edit { it[KEY_WEATHER_API_KEY] = key }
    }

    suspend fun setCity(name: String, lat: Double, lon: Double) {
        dataStore.edit {
            it[KEY_CITY_NAME] = name
            it[KEY_CITY_LAT] = lat
            it[KEY_CITY_LON] = lon
        }
    }

    suspend fun setDefaultFlowRate(rate: String) {
        dataStore.edit { it[KEY_DEFAULT_FLOW_RATE] = rate }
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        dataStore.edit { it[KEY_DARK_THEME] = if (enabled) "true" else "false" }
    }

    companion object {
        private val KEY_WEATHER_API_KEY = stringPreferencesKey("weather_api_key")
        private val KEY_CITY_NAME = stringPreferencesKey("city_name")
        private val KEY_CITY_LAT = doublePreferencesKey("city_lat")
        private val KEY_CITY_LON = doublePreferencesKey("city_lon")
        private val KEY_DEFAULT_FLOW_RATE = stringPreferencesKey("default_flow_rate")
        private val KEY_DARK_THEME = stringPreferencesKey("dark_theme")
    }
}
