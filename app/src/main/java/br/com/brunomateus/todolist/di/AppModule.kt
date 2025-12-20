package br.com.brunomateus.todolist.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import br.com.brunomateus.todolist.data.AppDatabase
import br.com.brunomateus.todolist.data.repository.TaskRepository
import br.com.brunomateus.todolist.data.repository.UserPreferencesRepository
import br.com.brunomateus.todolist.ui.TodoListViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
val appModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "my_app_db"
        ).fallbackToDestructiveMigration(dropAllTables = true).build()
    }
    single {
        get<AppDatabase>().taskDao()
    }
    single {
       androidContext().dataStore
    }
    single {
        UserPreferencesRepository(dataStore = get())
    }
    single {
        TaskRepository(dao = get())
    }
    viewModel {
        TodoListViewModel(taskRepository = get(), userPreferencesRepository = get())
    }

}