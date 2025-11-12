package br.com.brunomateus.todolist.ui

import androidx.lifecycle.ViewModel
import br.com.brunomateus.todolist.model.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TodoListViewModel() : ViewModel() {

    private val _uiState = MutableStateFlow(TodoListUiState())
    val uiState = _uiState.asStateFlow()


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
}