package br.com.brunomateus.todolist.data.repository

import br.com.brunomateus.todolist.data.dao.SortOrder
import br.com.brunomateus.todolist.data.dao.TaskDao
import br.com.brunomateus.todolist.model.Category
import br.com.brunomateus.todolist.model.Task
import br.com.brunomateus.todolist.ui.VisualizationOption
import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val dao: TaskDao
) {

    fun getAll(
        sortOrder: SortOrder = SortOrder.NONE,
        visualization: VisualizationOption = VisualizationOption.ALL,
        categories: Set<Category> = emptySet()
    ): Flow<List<Task>> = when (visualization) {
        VisualizationOption.ALL -> dao.getAll(
            sortOrder = sortOrder.name,
            categories = categories.map { it.name })

        VisualizationOption.NOT_CONCLUDED -> dao.getAllNotCompleted(
            sortOrder = sortOrder.name,
            categories = categories.map { it.name })
    }


    suspend fun addTask(task: Task) = dao.add(task)

    suspend fun deleteTask(task: Task) = dao.delete(task)

    suspend fun deleteTasks(vararg tasks: Task) = dao.deleteMany(*tasks)

    suspend fun toggleComplete(task: Task) =
        dao.toggleComplete(task.copy(isCompleted = !task.isCompleted))
}