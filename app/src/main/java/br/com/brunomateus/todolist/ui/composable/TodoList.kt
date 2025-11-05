package br.com.brunomateus.todolist.ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.brunomateus.todolist.model.Category
import br.com.brunomateus.todolist.model.Task


@Composable
fun TodoList(tasks: List<Task>, modifier: Modifier = Modifier) {
    LazyColumn (
        modifier = modifier.background(MaterialTheme.colorScheme.background).fillMaxSize()
    ) {
        items(tasks) { task ->
            TodoListItem(task, modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp))
        }
    }
}

@Composable
fun TodoListItem(task: Task, modifier: Modifier = Modifier) {
    var isToggled by remember { mutableStateOf(false) }
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
                    checked = false,
                    onCheckedChange = { },
                    colors = CheckboxDefaults.colors(
                        uncheckedColor = task.category.color,
                        checkedColor = task.category.color
                    )
                )
                Column (
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "#${task.category.getName()}",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            IconButton(
                onClick = {},
            ) {
                Icon(
                    imageVector = if (isToggled) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    tint = if (isToggled) Color.Red else MaterialTheme.colorScheme.secondary,
                    contentDescription = if (isToggled) "Selected icon button" else "Unselected icon button."
                )
            }
        }
    }

}

@Preview
@Composable
fun PreviewTodoList() {
    val tasks = listOf<Task>(
        Task("Teste 1", Category.SAUDE),
        Task("Teste 2", Category.ESTUDO),
        Task("Teste 3", Category.LAZER),
        Task("Teste 4", Category.TRABALHO)
    )
    TodoList(tasks)
}

@Preview
@Composable
fun PreviewListItem() {
    TodoListItem(Task("Fazer exame de sangue", Category.SAUDE))
}