package br.com.brunomateus.todolist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
fun TodoTopBar() {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = {
            Text(
                text = stringResource(R.string.todo_header),
                fontWeight = FontWeight.Bold
            )
        },
        actions = {
            IconButton(onClick = { /* do something */ }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.visualization_options)
                )
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

@Composable
fun TodoMainScreen(modifier: Modifier = Modifier) {

    var showDialog by remember { mutableStateOf(false) }

    val tasks = remember { mutableStateListOf<Task>(
        Task("Teste 1", Category.SAUDE),
        Task("Teste 2", Category.ESTUDO),
        Task("Teste 3", Category.LAZER),
        Task("Teste 4", Category.TRABALHO)
    )}
    Scaffold(
        topBar = { TodoTopBar() },
        floatingActionButton = {
            TodoFloatActionButton(onClick = {
                showDialog = true
            })
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        TodoList(tasks = tasks, modifier = modifier.padding(innerPadding))
    }

    AnimatedVisibility(showDialog) {
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
    modifier: Modifier = Modifier) {

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
                onClick = { onTaskAdd(Task(text.trim(), selectedOption)) },
                enabled = text.trim().isNotBlank()
            ) {
                Text(stringResource(R.string.add_task_button_text))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(stringResource(R.string.dismiss_dialog_button_text))
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
