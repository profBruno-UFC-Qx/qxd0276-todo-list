package br.com.brunomateus.todolist.ui

import br.com.brunomateus.todolist.model.Category
import java.util.UUID

enum class SortOrder {
    NONE, ASCENDING, DESCENDING
}

enum class VisualizationOption {
    ALL, NOT_CONCLUDED
}

data class TodoListUiState(
    val sortOrder: SortOrder = SortOrder.NONE,
    val selectedCategories: Set<Category> = setOf(),
    val visualizationOption: VisualizationOption = VisualizationOption.ALL,
    val selectedTaskIds: Set<UUID> = emptySet(),
) {
    val inSelectionMode: Boolean
        get() = selectedTaskIds.isNotEmpty()
}