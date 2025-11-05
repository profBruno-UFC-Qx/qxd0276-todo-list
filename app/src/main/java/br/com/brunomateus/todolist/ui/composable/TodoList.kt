package br.com.brunomateus.todolist.ui.composable

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.brunomateus.todolist.model.Category
import br.com.brunomateus.todolist.model.Task


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TodoList(
    tasks: List<Task>,
    onTaskCompleted: (Task, Boolean) -> Unit,
    onDeleteTask: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
    ) {
        items(tasks, key = { it.id }) { task ->
            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = {
                    when (it) {
                        SwipeToDismissBoxValue.EndToStart -> {
                            onDeleteTask(task)
                            true
                        }

                        SwipeToDismissBoxValue.StartToEnd -> {
                            onTaskCompleted(task, !task.isCompleted)
                            false // Do not dismiss, just toggle
                        }

                        else -> false
                    }
                }
            )

            // Reset the dismiss state when the swipe is not in progress
            LaunchedEffect(dismissState.currentValue) {
                if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                    dismissState.reset()
                }
            }

            SwipeToDismissBox(
                state = dismissState,
                modifier = Modifier.animateItemPlacement(tween(250)),
                backgroundContent = {
                    val color = when (dismissState.targetValue) {
                        SwipeToDismissBoxValue.EndToStart -> Color.Red.copy(alpha = 0.8f)
                        SwipeToDismissBoxValue.StartToEnd -> Color.Green.copy(alpha = 0.8f)
                        else -> MaterialTheme.colorScheme.background
                    }
                    val icon = when (dismissState.targetValue) {
                        SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                        SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Check
                        else -> null
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color, shape = RoundedCornerShape(5.dp))
                            .padding(horizontal = 16.dp),
                        contentAlignment = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        icon?.let {
                            Icon(it, contentDescription = null, tint = Color.White)
                        }
                    }
                }
            ) {
                TodoListItem(
                    task = task,
                    onTaskCompleted = { onTaskCompleted(task, it) },
                    onDeleteTask = { onDeleteTask(task) },
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun TodoListItem(
    task: Task,
    onTaskCompleted: (Boolean) -> Unit,
    onDeleteTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(5.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = onTaskCompleted,
                    colors = CheckboxDefaults.colors(
                        uncheckedColor = task.category.color,
                        checkedColor = task.category.color
                    )
                )
                Column(
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyLarge,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                    Text(
                        text = "#${task.category.getName()}",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            IconButton(onClick = onDeleteTask) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    tint = MaterialTheme.colorScheme.secondary,
                    contentDescription = "Delete task button"
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewTodoList() {
    val tasks = listOf(
        Task("Teste 1", Category.SAUDE),
        Task("Teste 2", Category.ESTUDO, isCompleted = true),
        Task("Teste 3", Category.LAZER),
        Task("Teste 4", Category.TRABALHO)
    )
    TodoList(tasks, { _, _ -> }, {})
}

@Preview
@Composable
fun PreviewListItem() {
    TodoListItem(Task("Fazer exame de sangue", Category.SAUDE), {}, {})
}
