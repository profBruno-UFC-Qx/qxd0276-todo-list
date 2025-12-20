package br.com.brunomateus.todolist.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.core.IOException
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import br.com.brunomateus.todolist.data.dao.SortOrder
import br.com.brunomateus.todolist.ui.VisualizationOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map


data class UserSettings(val sortOrder: SortOrder, val showAll: VisualizationOption)

class UserPreferencesRepository(private val dataStore: DataStore<Preferences>){

    private companion object PreferencesKeys {
        val SORT_ORDER = stringPreferencesKey("sort_preference")
        val SHOW_ALL = stringPreferencesKey("show_all")
    }

    val userSettings: Flow<UserSettings> = dataStore.data
        .catch { exception ->
            UserSettings(sortOrder = SortOrder.NONE, showAll = VisualizationOption.ALL)
        }
        .map { preferences ->
            val sort = preferences[SORT_ORDER]
            val showAll = preferences[SHOW_ALL]
            UserSettings(
                sortOrder = if (sort != null) SortOrder.valueOf(sort) else SortOrder.NONE,
                showAll = if (showAll != null) VisualizationOption.valueOf(showAll) else VisualizationOption.ALL
            )
        }

    suspend fun changeOrder(newOrder: SortOrder) {
        dataStore.edit { preferences ->
            preferences[SORT_ORDER] = newOrder.name
        }
    }

    suspend fun toggleCompleteness(visualizationOption: VisualizationOption) {
        dataStore.edit { preferences ->
            preferences[SHOW_ALL] = visualizationOption.name
        }
    }
}