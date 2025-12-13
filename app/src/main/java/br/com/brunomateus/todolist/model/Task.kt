package br.com.brunomateus.todolist.model

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Category(val color: Color) {
    ESTUDO(Color.Red) {
        override fun getName(): String = "Estudo"
    }, LAZER(Color.Cyan) {
        override fun getName(): String  = "Lazer"
    }, TRABALHO(Color.Gray) {
        override fun getName(): String = "Trabalho"
    }, SAUDE(Color.Green) {
        override fun getName(): String = "Saúde"
    };
    abstract fun getName(): String
}

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val description: String,
    val category: Category,
    val isCompleted: Boolean = false
)