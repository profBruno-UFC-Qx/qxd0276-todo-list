package br.com.brunomateus.todolist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
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
                Scaffold(
                    topBar = { TodoTopBar() },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    TodoMainScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
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
                text= stringResource(R.string.todo_header),
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
fun TodoMainScreen(modifier: Modifier = Modifier) {
    val tasks = listOf<Task>(
        Task("Teste 1", Category.SAUDE),
        Task("Teste 2", Category.ESTUDO),
        Task("Teste 3", Category.LAZER),
        Task("Teste 4", Category.TRABALHO)
    )
    TodoList(tasks = tasks, modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TodolistTheme {
        TodoMainScreen()
    }
}