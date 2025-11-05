package br.com.brunomateus.todolist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.brunomateus.todolist.model.Category
import br.com.brunomateus.todolist.model.Task
import br.com.brunomateus.todolist.ui.composable.TodoList
import br.com.brunomateus.todolist.ui.theme.TodolistTheme
import java.util.UUID

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
    isFiltered: Boolean, 
    onFilterChange: (Boolean) -> Unit,
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
                Text("$selectedCount selecionadas")
            }
        },
        navigationIcon = {
            if (inSelectionMode) {
                IconButton(onClick = onClearSelection) {
                    Icon(Icons.Default.Close, contentDescription = "Fechar modo de seleção")
                }
            }
        },
        actions = {
            if (inSelectionMode) {
                IconButton(onClick = onDeleteSelected) {
                    Icon(Icons.Default.Delete, contentDescription = "Deletar tarefas selecionadas")
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
                            text = { Text("Todas as tarefas") },
                            onClick = {
                                onFilterChange(false)
                                expanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Somente não concluídas") },
                            onClick = {
                                onFilterChange(true)
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
fun TodoFloatActionButton(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = { onClick() },
    ) {
        Icon(Icons.Filled.Add, stringResource(R.string.add_a_new_task_button))
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
        items(Category.values()) { category ->
            FilterChip(
                selected = category in selectedCategories,
                onClick = { onCategorySelected(category) },
                label = { Text(category.getName()) }
            )
        }
    }
}


@Composable
fun TodoMainScreen(modifier: Modifier = Modifier) {

    var showDialog by remember { mutableStateOf(false) }
    var isFiltered by remember { mutableStateOf(false) }
    var selectedCategories by remember { mutableStateOf(emptySet<Category>()) }
    var selectedTaskIds by remember { mutableStateOf<Set<UUID>>(emptySet()) }
    val inSelectionMode = selectedTaskIds.isNotEmpty()


    val tasks = remember {
        mutableStateListOf(
            Task("Teste 1", Category.SAUDE),
            Task("Teste 2", Category.ESTUDO),
            Task("Teste 3", Category.LAZER),
            Task("Teste 4", Category.TRABALHO)
        )
    }
    Scaffold(
        topBar = { 
            TodoTopBar(
                isFiltered = isFiltered, 
                onFilterChange = { isFiltered = it },
                inSelectionMode = inSelectionMode,
                selectedCount = selectedTaskIds.size,
                onClearSelection = { selectedTaskIds = emptySet() },
                onDeleteSelected = {
                    tasks.removeAll { it.id in selectedTaskIds }
                    selectedTaskIds = emptySet()
                }
            )
        },
        floatingActionButton = {
            if (!inSelectionMode) {
                TodoFloatActionButton(onClick = {
                    showDialog = true
                })
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            if (!inSelectionMode) {
                CategoryFilter(
                    selectedCategories = selectedCategories,
                    onCategorySelected = { category ->
                        selectedCategories = if (category in selectedCategories) {
                            selectedCategories - category
                        } else {
                            selectedCategories + category
                        }
                    }
                )
            }
            val filteredTasks = tasks.filter { task ->
                val completionFilter = !isFiltered || !task.isCompleted
                val categoryFilter = selectedCategories.isEmpty() || task.category in selectedCategories
                completionFilter && categoryFilter
            }
            TodoList(
                tasks = filteredTasks,
                inSelectionMode = inSelectionMode,
                selectedTaskIds = selectedTaskIds,
                onTaskClick = { task ->
                    if (inSelectionMode) {
                        selectedTaskIds = if (task.id in selectedTaskIds) {
                            selectedTaskIds - task.id
                        } else {
                            selectedTaskIds + task.id
                        }
                    } else {
                        // Handle regular click here if needed
                    }
                },
                onTaskLongClick = { task -> 
                    if (!inSelectionMode) {
                        selectedTaskIds = setOf(task.id)
                    }
                },
                onTaskCompleted = { task, isCompleted ->
                    val index = tasks.indexOf(task)
                    if (index != -1) {
                        tasks[index] = task.copy(isCompleted = isCompleted)
                    }
                },
                onDeleteTask = { task -> tasks.remove(task) },
                modifier = modifier
            )
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
fun AddTaskDialog(
    onDismissRequest: () -> Unit,
    onTaskAdd: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = Category.values().toList()
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(options[0]) }
    var text by remember { mutableStateOf("") }

    AlertDialog(
        title = {
            Text(
                text = stringResource(R.string.add_a_new_task),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Row() {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        label = { Text(stringResource(R.string.add_task_field_label)) },
                        placeholder = { Text(text = stringResource(R.string.add_task_field_placeholder)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Column(modifier.selectableGroup()) {
                    options.forEach { category ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .selectable(
                                    selected = (category == selectedOption),
                                    onClick = { onOptionSelected(category) },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (category == selectedOption),
                                onClick = null // null recommended for accessibility with screen readers
                            )
                            Text(
                                text = category.getName(),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }
            }
        },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = { onTaskAdd(Task(text, selectedOption)) },
            ) {
                Text("Adicionar")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text("Cancelar")
            }
        },
        modifier = modifier
    )
}

@Preview
@Composable
fun PreviewDialog() {
    AddTaskDialog({}, {})
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TodolistTheme {
        TodoMainScreen()
    }
}
