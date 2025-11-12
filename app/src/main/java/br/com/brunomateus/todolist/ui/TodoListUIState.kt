package br.com.brunomateus.todolist.ui

import br.com.brunomateus.todolist.model.Category

enum class SortOrder {
    NONE, ASCENDING, DESCENDING
}

data class TodoListUiState(
    val sortOrder: SortOrder = SortOrder.NONE,
    val selectedCategories: Set<Category> = setOf()

)