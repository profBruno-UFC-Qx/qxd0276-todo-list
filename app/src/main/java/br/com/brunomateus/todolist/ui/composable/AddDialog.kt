package br.com.brunomateus.todolist.ui.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import br.com.brunomateus.todolist.R
import br.com.brunomateus.todolist.model.Category
import br.com.brunomateus.todolist.model.Task

@Composable
fun AddTaskDialog(
    onDismissRequest: () -> Unit,
    onTaskAdd: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    val options = Category.entries.toList()
    val (selectedOption, onOptionSelected) = rememberSaveable { mutableStateOf(options[0]) }
    var text by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        title = {
            Text(
                text = stringResource(R.string.add_a_new_task),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Row {
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
                enabled = text.isNotBlank()
            ) {
                Text(
                    text = stringResource(R.string.add_task_button_text)
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text(
                    text = stringResource(R.string.dismiss_dialog_button_text)
                )
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