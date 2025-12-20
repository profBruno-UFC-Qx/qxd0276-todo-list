package br.com.brunomateus.todolist.data

import androidx.room.Database
import androidx.room.RoomDatabase
import br.com.brunomateus.todolist.data.dao.TaskDao
import br.com.brunomateus.todolist.model.Task

@Database(entities = [Task::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}