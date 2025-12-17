package br.com.brunomateus.todolist.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import br.com.brunomateus.todolist.data.dao.SortOrder
import br.com.brunomateus.todolist.ui.VisualizationOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object PreferencesKeys {
    val SORT_ORDER = stringPreferencesKey("sort_preference")
    val SHOW_ALL = booleanPreferencesKey("show_all")
}

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>){

    val sortOrder: Flow<String> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SORT_ORDER] ?: SortOrder.NONE.name
    }

    val showAll: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SHOW_ALL] ?: true
    }

    suspend fun changeOrder(newOrder: SortOrder) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SORT_ORDER] = newOrder.name
        }
    }

    suspend fun toggleCompleteness(visualizationOption: VisualizationOption) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_ALL] = visualizationOption == VisualizationOption.ALL

        }
    }
}