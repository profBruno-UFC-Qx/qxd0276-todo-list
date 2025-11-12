package br.com.brunomateus.todolist.ui

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.ViewModel
import br.com.brunomateus.todolist.model.Category
import br.com.brunomateus.todolist.model.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class TodoListViewModel() : ViewModel() {

    private val _uiState = MutableStateFlow(TodoListUiState())
    val uiState = _uiState.asStateFlow()

    private val _tasks =  MutableStateFlow<List<Task>>(emptyList())
    val tasks = _tasks.asStateFlow()


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
        _uiState.update {
            currentState -> currentState.copy(selectedCategories = newSelection)
        }
    }

    fun sort() {
        _uiState.update { currentState ->
            currentState.copy(
                sortOrder = when (_uiState.value.sortOrder) {
                    SortOrder.NONE ->  SortOrder.ASCENDING
                    SortOrder.ASCENDING -> SortOrder.DESCENDING
                    SortOrder.DESCENDING -> SortOrder.NONE
                }
            )
        }
    }

    fun add(task: Task) {
        _tasks.value = _tasks.value + task
    }
    fun removeAll(selectedTaskIds: Set<UUID>) {
        _tasks.value = _tasks.value.filter { it -> it.id !in selectedTaskIds }
    }

    fun remove(task: Task) {
        _tasks.value = _tasks.value.filter { it -> it.id != task.id }
    }

    fun toogleComplete(task: Task) {
        _tasks.value = _tasks.value.map { t ->
            if (t.id == task.id) {
                t.copy(isCompleted = !t.isCompleted)
            } else {
                t
            }
        }
    }



}