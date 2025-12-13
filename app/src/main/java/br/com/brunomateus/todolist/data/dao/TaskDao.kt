package br.com.brunomateus.todolist.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import br.com.brunomateus.todolist.model.Task
import kotlinx.coroutines.flow.Flow

enum class SortOrder {
    ASC, DESC, NONE
}

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks WHERE " +
        "(:categoriesCount = 0 OR category IN (:categories)) " +
        "ORDER BY " +
        "CASE WHEN :sortOrder = 'NONE' THEN id END, " +
        "CASE WHEN :sortOrder = 'ASC' THEN description END ASC, " +
        "CASE WHEN :sortOrder = 'DESC' THEN description END DESC")
    fun getAll(
        sortOrder: String,
        categories: List<String>,
        categoriesCount: Int = categories.size
    ): Flow<List<Task>>


    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND " +
            "(:categoriesCount = 0 OR category IN (:categories)) " +
            "ORDER BY " +
            "CASE WHEN :sortOrder = 'NONE' THEN id END, " +
            "CASE WHEN :sortOrder = 'ASC' THEN description END ASC, " +
            "CASE WHEN :sortOrder = 'DESC' THEN description END DESC")
    fun getAllNotCompleted(
        sortOrder: String,
        categories: List<String>,
        categoriesCount: Int = categories.size
    ): Flow<List<Task>>


    @Insert
    suspend fun add(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Delete
    suspend fun deleteMany(vararg task: Task)

    @Update
    suspend fun toggleComplete(task: Task)
}
