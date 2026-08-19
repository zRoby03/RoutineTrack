package com.example.routinetrack.domain.model

enum class HabitCategory(val label: String, val icon: String) {
    HEALTH("Salute", "favorite"),
    FITNESS("Allenamento", "fitness"),
    STUDY("Studio", "school"),
    MINDFULNESS("Mente", "spa"),
    WATER("Acqua", "water"),
    OTHER("Altro", "star")
}
