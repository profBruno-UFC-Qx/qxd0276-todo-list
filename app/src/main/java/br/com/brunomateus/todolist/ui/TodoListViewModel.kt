package br.com.brunomateus.todolist.ui


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.brunomateus.todolist.data.dao.SortOrder
import br.com.brunomateus.todolist.data.repository.TaskRepository
import br.com.brunomateus.todolist.data.repository.UserPreferencesRepository
import br.com.brunomateus.todolist.model.Category
import br.com.brunomateus.todolist.model.Task
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TodoListViewModel(
    private val taskRepository: TaskRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TodoListUiState())

    private val allTasksFromDb = taskRepository.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            userPreferencesRepository.userSettings.collect {
                _uiState.update { currentState ->
                    currentState.copy(
                        sortOrder = it.sortOrder,
                        visualizationOption = it.showAll
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val tasks = _uiState.flatMapLatest { uiState ->
        taskRepository.getAll(
            sortOrder = uiState.sortOrder,
            visualization = uiState.visualizationOption,
            categories = uiState.selectedCategories
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val uiState = combine(allTasksFromDb, tasks, _uiState) { allTasks, filteredTasks, uiState ->
        when {
            allTasks.isEmpty() -> uiState.copy(status = TodoListState.NoTaskRegistered)
            allTasks.all { it.isCompleted } -> uiState.copy(status = TodoListState.AllTasksConcluded)
            uiState.selectedTaskIds.isNotEmpty() -> uiState.copy(status = TodoListState.SelectionMode)
            filteredTasks.isEmpty() -> uiState.copy(status = TodoListState.NoTasksToShow)
            else -> uiState.copy(status = TodoListState.TaskToShow)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = _uiState.value
    )

    val completedTask = allTasksFromDb.map { tasks -> tasks.count { it.isCompleted } }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = 0
    )

    val totalTask = allTasksFromDb.map { it.size }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = 0
    )

    fun changeVisualization() {
        viewModelScope.launch {
            userPreferencesRepository.toggleCompleteness(
                when (_uiState.value.visualizationOption) {
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
        viewModelScope.launch {
            userPreferencesRepository.changeOrder(
                when (_uiState.value.sortOrder) {
                    SortOrder.NONE -> SortOrder.ASC
                    SortOrder.ASC -> SortOrder.DESC
                    SortOrder.DESC -> SortOrder.NONE
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

    fun add(description: String, category: Category) {
        viewModelScope.launch {
            taskRepository.addTask(
                Task(
                    category = category,
                    description = description
                )
            )
        }
    }

    fun removeAll() {
        val selectedTaskIds = _uiState.value.selectedTaskIds
        viewModelScope.launch {
            tasks.value.filter { task -> task.id in selectedTaskIds }
                .also { taskRepository.deleteTasks(*it.toTypedArray()) }
            clearSelection()
        }
    }

    fun remove(task: Task) {
        viewModelScope.launch {
            taskRepository.deleteTask(task)
        }
    }

    fun toggleComplete(task: Task) {
        viewModelScope.launch {
            taskRepository.toggleComplete(task)
        }
    }
}
