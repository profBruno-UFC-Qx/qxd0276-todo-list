package br.com.brunomateus.todolist.data.repository

import br.com.brunomateus.todolist.data.dao.TaskDao
import br.com.brunomateus.todolist.model.Task
import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val dao: TaskDao
) {

    val tasks: Flow<List<Task>> = dao.getAll()

    suspend fun addTask(task: Task) = dao.add(task)

    suspend fun deleteTask(task: Task) = dao.delete(task)

    suspend fun deleteTasks(vararg tasks: Task) = dao.deleteMany(*tasks)

    suspend fun toggleComplete(task: Task) = dao.toggleComplete(task.copy(isCompleted = !task.isCompleted))
}