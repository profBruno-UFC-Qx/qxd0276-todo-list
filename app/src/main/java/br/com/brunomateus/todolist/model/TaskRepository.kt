package br.com.brunomateus.todolist.model

import androidx.compose.ui.graphics.Color

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

data class Task(val description: String, val category: Category)