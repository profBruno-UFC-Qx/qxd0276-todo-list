package br.com.brunomateus.todolist.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.brunomateus.todolist.model.Category
import br.com.brunomateus.todolist.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class TodoListViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TodoListUiState())

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())

    val tasks = combine(_tasks, _uiState) { tasks, uiState ->
        tasks.filter { task ->
            val completionFilter =
                uiState.visualizationOption == VisualizationOption.ALL || !task.isCompleted
            val categoryFilter =
                uiState.selectedCategories.isEmpty() || task.category in uiState.selectedCategories
            completionFilter && categoryFilter
        }.let { tasksToSort ->
            when (uiState.sortOrder) {
                SortOrder.ASCENDING -> tasksToSort.sortedBy { it.description }
                SortOrder.DESCENDING -> tasksToSort.sortedByDescending { it.description }
                SortOrder.NONE -> tasksToSort
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList()
    )

    val uiState = combine(_tasks, tasks, _uiState) { currentTasks, filtered, uiState ->
        when {
            currentTasks.isEmpty() -> uiState.copy(status = TodoListState.NoTaskRegistered)
            currentTasks.all { it -> it.isCompleted } -> uiState.copy(status = TodoListState.AllTasksConcluded)
            uiState.selectedTaskIds.isNotEmpty() -> uiState.copy(status = TodoListState.SelectionMode)
            filtered.isEmpty() -> uiState.copy(status = TodoListState.NoTasksToShow)
            else -> uiState.copy(status = TodoListState.TaskToShow)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = _uiState.value
    )

    val completedTask = _tasks.map { tasks -> tasks.count { it.isCompleted } }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = 0
    )

    val totalTask = _tasks.map { it.size }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = 0
    )

    fun changeVisualization() {
        _uiState.update { currentState ->
            currentState.copy(
                visualizationOption = when (_uiState.value.visualizationOption) {
                    VisualizationOption.ALL -> VisualizationOption.NOT_CONCLUDED
                    VisualizationOption.NOT_CONCLUDED -> VisualizationOption.ALL
                }
            )
        }
    }

    fun onCategorySelected(category: Category) {
        val current = _uiState.value.selectedCategories
        val newSelection = if (category in current) {
            current - category
        } else {
            current + category
        }
        _uiState.update { currentState -> currentState.copy(selectedCategories = newSelection) }
    }

    fun sort() {
        _uiState.update { currentState ->
            currentState.copy(
                sortOrder = when (_uiState.value.sortOrder) {
                    SortOrder.NONE -> SortOrder.ASCENDING
                    SortOrder.ASCENDING -> SortOrder.DESCENDING
                    SortOrder.DESCENDING -> SortOrder.NONE
                }
            )
        }
    }

    fun onTaskClick(task: Task) {
        val currentSelection = _uiState.value.selectedTaskIds
        val newSelection = if (task.id in currentSelection) {
            currentSelection - task.id
        } else {
            currentSelection + task.id
        }
        _uiState.update { it.copy(selectedTaskIds = newSelection) }
    }

    fun onTaskLongClick(task: Task) {
        _uiState.update { it.copy(selectedTaskIds = setOf(task.id)) }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedTaskIds = emptySet()) }
    }

    fun add(task: Task) {
        _tasks.value = _tasks.value + task
    }

    fun removeAll() {
        val selectedTaskIds = _uiState.value.selectedTaskIds
        _tasks.value = _tasks.value.filter { it.id !in selectedTaskIds }
        clearSelection()
    }

    fun remove(task: Task) {
        _tasks.value = _tasks.value.filter { it.id != task.id }
    }

    fun toggleComplete(task: Task) {
        _tasks.value = _tasks.value.map { t ->
            if (t.id == task.id) {
                t.copy(isCompleted = !t.isCompleted)
            } else {
                t
            }
        }
    }
}
