package com.evani.homechefai.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.evani.homechefai.data.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// ... rest of PreferencesManager code
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
class PreferencesManager(private val context: Context) {
    companion object {
        private val DIET_TYPE = stringPreferencesKey("diet_type")
        private val CUISINE = stringPreferencesKey("cuisine")
        private val COUNTRY = stringPreferencesKey("country")
        private val REGION = stringPreferencesKey("region")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .map { preferences ->
            UserPreferences(
                dietType = preferences[DIET_TYPE] ?: "",
                cuisine = preferences[CUISINE] ?: "",
                country = preferences[COUNTRY] ?: "",
                region = preferences[REGION] ?: ""
            )
        }

    suspend fun updatePreferences(userPreferences: UserPreferences) {
        context.dataStore.edit { preferences ->
            preferences[DIET_TYPE] = userPreferences.dietType
            preferences[CUISINE] = userPreferences.cuisine
            preferences[COUNTRY] = userPreferences.country
            preferences[REGION] = userPreferences.region
        }
    }

}