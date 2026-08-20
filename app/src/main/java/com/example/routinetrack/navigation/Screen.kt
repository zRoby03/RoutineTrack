package com.example.routinetrack.navigation

sealed class Screen(val route: String, val title: String) {
    object Login : Screen("login", "Accedi")
    object Register : Screen("register", "Registrati")
    object Home : Screen("home", "Oggi")
    object AddHabit : Screen("add_habit?habitId={habitId}", "Aggiungi") {
        const val baseRoute = "add_habit"
        fun createRoute(habitId: Long? = null): String {
            return if (habitId == null) baseRoute else "$baseRoute?habitId=$habitId"
        }
    }
    object HabitDetail : Screen("habit_detail/{habitId}", "Dettaglio") {
        fun createRoute(habitId: Long): String = "habit_detail/$habitId"
    }
    object Stats : Screen("stats", "Statistiche")
    object Settings : Screen("settings", "Impostazioni")
}
