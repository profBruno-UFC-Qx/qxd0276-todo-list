
package br.com.brunomateus.todolist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.brunomateus.todolist.model.Category
import br.com.brunomateus.todolist.model.Task
import br.com.brunomateus.todolist.ui.TodoListViewModel
import br.com.brunomateus.todolist.ui.composable.AddTaskDialog
import br.com.brunomateus.todolist.ui.composable.TodoList
import br.com.brunomateus.todolist.ui.screen.AllTasksCompletedScreen
import br.com.brunomateus.todolist.ui.screen.NoTasksFoundScreen
import br.com.brunomateus.todolist.ui.screen.NoTasksScreen
import br.com.brunomateus.todolist.ui.theme.TodolistTheme
import kotlinx.coroutines.launch
import java.util.UUID
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.brunomateus.todolist.ui.SortOrder
import br.com.brunomateus.todolist.ui.VisualizationOption


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TodolistTheme {
                TodoMainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoTopBar(
    visualization: VisualizationOption,
    onFilterChange: () -> Unit,
    sortOrder: SortOrder,
    onSortOrderChange: () -> Unit,
    inSelectionMode: Boolean,
    selectedCount: Int,
    onClearSelection: () -> Unit,
    onDeleteSelected: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = if (inSelectionMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.background,
            titleContentColor = if (inSelectionMode) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary,
        ),
        title = {
            if (!inSelectionMode) {
                Text(
                    text = stringResource(R.string.todo_header),
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(stringResource(R.string.number_of_selected_tasks, selectedCount))
            }
        },
        navigationIcon = {
            if (inSelectionMode) {
                IconButton(onClick = onClearSelection) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.close_selection_mode_button_description)
                    )
                }
            }
        },
        actions = {
            if (inSelectionMode) {
                IconButton(onClick = onDeleteSelected) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete_button_description)
                    )
                }
            } else {
                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.visualization_options)
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (visualization == VisualizationOption.NOT_CONCLUDED) stringResource(R.string.all_tasks) else stringResource(
                                        R.string.only_todo_tasks
                                    )
                                )
                            },
                            onClick = {
                                onFilterChange()
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                val text = when (sortOrder) {
                                    SortOrder.ASCENDING -> stringResource(R.string.sort_z_a)
                                    SortOrder.DESCENDING -> stringResource(R.string.no_order)
                                    else -> stringResource(R.string.sort_a_z)
                                }
                                Text(text)
                            },
                            onClick = {
                                onSortOrderChange()
                                expanded = false
                            }
                        )
                    }
                }
            }
        },
    )
}

@Composable
fun AddTaskFloatActionButton(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
    ) {
        Icon(Icons.Filled.Add, stringResource(R.string.add_a_new_task_button))
    }
}

@Composable
fun GoToTopFloatActionButton(
    listState: LazyListState
) {
    val scope = rememberCoroutineScope()
    FloatingActionButton(
        onClick = {
            scope.launch {
                listState.animateScrollToItem(0)
            }
        },
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Ir para o topo")
    }
}

@Composable
fun TodoMainScreen(modifier: Modifier = Modifier, viewModel: TodoListViewModel = viewModel()) {

    val todolistUiState by viewModel.uiState.collectAsState()

    var showDialog by rememberSaveable { mutableStateOf(false) }
    val selectedTaskIds = remember { mutableStateSetOf<UUID>() }
    val inSelectionMode = selectedTaskIds.isNotEmpty()
    val listState = rememberLazyListState()
    val showScrollToTopButton by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }

    val tasks = rememberSaveable {
        mutableStateListOf<Task>()
    }

    val completedTasks = tasks.count { it.isCompleted }
    val totalTasks = tasks.size
    val allTasksCompleted = tasks.isNotEmpty() && tasks.all { it.isCompleted }

    Scaffold(
        topBar = {
            TodoTopBar(
                visualization = todolistUiState.visualizationOption,
                onFilterChange = { viewModel.changeVisualization() },
                sortOrder = todolistUiState.sortOrder,
                onSortOrderChange = { viewModel.sort()},
                inSelectionMode = inSelectionMode,
                selectedCount = selectedTaskIds.size,
                onClearSelection = { selectedTaskIds.clear() },
                onDeleteSelected = {
                    tasks.removeAll { it.id in selectedTaskIds }
                    selectedTaskIds.clear()
                }
            )
        },
        bottomBar = {
            if (!inSelectionMode) {
                TaskProgressBar(completedTasks = completedTasks, totalTasks = totalTasks)
            }
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                if (showScrollToTopButton) {
                    GoToTopFloatActionButton(listState)
                }
                if (!inSelectionMode) {
                    AddTaskFloatActionButton(onClick = {
                        showDialog = true
                    })
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            when {
                tasks.isEmpty() -> NoTasksScreen()
                allTasksCompleted && todolistUiState.visualizationOption == VisualizationOption.NOT_CONCLUDED -> AllTasksCompletedScreen()
                else -> FilteredTaskList(
                    tasks = tasks,
                    listState = listState,
                    inSelectionMode = inSelectionMode,
                    visualization = todolistUiState.visualizationOption,
                    sortOrder = todolistUiState.sortOrder,
                    selectedCategories = todolistUiState.selectedCategories,
                    onCategorySelected = { viewModel.onCategorySelected(it)},
                    selectedTaskIds = selectedTaskIds,
                    onTaskClick = { task ->
                        if (inSelectionMode) {
                            if (task.id in selectedTaskIds) {
                                selectedTaskIds.remove(task.id)
                            } else {
                                selectedTaskIds.add(task.id)
                            }
                        }
                    },
                    onTaskLongClick = { task ->
                        if (!inSelectionMode) {
                            selectedTaskIds.add(task.id)
                        }
                    },
                    onTaskCompleted = { task, isCompleted ->
                        val index = tasks.indexOfFirst { it.id == task.id }
                        if (index != -1) {
                            tasks[index] = task.copy(isCompleted = isCompleted)
                        }
                    },
                    onDeleteTask = { task -> tasks.remove(task) }
                )
            }
        }
    }

    if (showDialog) {
        AddTaskDialog(
            onDismissRequest = { showDialog = false },
            onTaskAdd = { task ->
                tasks.add(task)
                showDialog = false
            }
        )
    }
}

@Composable
fun FilteredTaskList(
    tasks: List<Task>,
    listState: LazyListState,
    inSelectionMode: Boolean,
    visualization: VisualizationOption,
    sortOrder: SortOrder,
    selectedCategories: Set<Category>,
    onCategorySelected: (Category) -> Unit,
    selectedTaskIds: Set<UUID>,
    onTaskClick: (Task) -> Unit,
    onTaskLongClick: (Task) -> Unit,
    onTaskCompleted: (Task, Boolean) -> Unit,
    onDeleteTask: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    Column {
        if (!inSelectionMode) {
            CategoryFilter(
                selectedCategories = selectedCategories,
                onCategorySelected = onCategorySelected
            )
        }
        val filteredTasks = tasks.filter { task ->
                val completionFilter = visualization == VisualizationOption.ALL || !task.isCompleted
                val categoryFilter =
                    selectedCategories.isEmpty() || task.category in selectedCategories
                completionFilter && categoryFilter
            }.let { tasksToSort ->
                when (sortOrder) {
                    SortOrder.ASCENDING -> tasksToSort.sortedBy { it.description }
                    SortOrder.DESCENDING -> tasksToSort.sortedByDescending { it.description }
                    SortOrder.NONE -> tasksToSort
                }
            }


        val filtersAreActive = selectedCategories.isNotEmpty() || visualization == VisualizationOption.NOT_CONCLUDED
        if (filteredTasks.isEmpty() && filtersAreActive) {
            NoTasksFoundScreen()
        } else {
            TodoList(
                tasks = filteredTasks,
                listState = listState,
                selectedTaskIds = selectedTaskIds,
                onTaskClick = onTaskClick,
                onTaskLongClick = onTaskLongClick,
                onTaskCompleted = onTaskCompleted,
                onDeleteTask = onDeleteTask,
                modifier = modifier
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFilter(
    selectedCategories: Set<Category>,
    onCategorySelected: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(Category.entries) { category ->
            FilterChip(
                selected = category in selectedCategories,
                onClick = { onCategorySelected(category) },
                label = { Text(category.getName()) }
            )
        }
    }
}

@Composable
fun TaskProgressBar(completedTasks: Int, totalTasks: Int, modifier: Modifier = Modifier) {
    val progressTarget = if (totalTasks > 0) completedTasks.toFloat() / totalTasks.toFloat() else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progressTarget,
        label = "progressAnimation"
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = stringResource(R.string.progress_bar_message, completedTasks, totalTasks),
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TodolistTheme {
        TodoMainScreen()
    }
}
