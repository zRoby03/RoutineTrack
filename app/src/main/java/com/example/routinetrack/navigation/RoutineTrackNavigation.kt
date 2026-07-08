package com.example.routinetrack.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.routinetrack.RoutineTrackApplication
import com.example.routinetrack.domain.model.AppThemeMode
import com.example.routinetrack.ui.components.BottomNavigationBar
import com.example.routinetrack.ui.screens.addhabit.AddHabitScreen
import com.example.routinetrack.ui.screens.addhabit.AddHabitViewModel
import com.example.routinetrack.ui.screens.auth.AuthViewModel
import com.example.routinetrack.ui.screens.auth.LoginScreen
import com.example.routinetrack.ui.screens.auth.PasswordResetScreen
import com.example.routinetrack.ui.screens.auth.RegisterScreen
import com.example.routinetrack.ui.screens.calendar.CalendarScreen
import com.example.routinetrack.ui.screens.calendar.CalendarViewModel
import com.example.routinetrack.ui.screens.detail.HabitDetailScreen
import com.example.routinetrack.ui.screens.detail.HabitDetailViewModel
import com.example.routinetrack.ui.screens.home.HomeScreen
import com.example.routinetrack.ui.screens.home.HomeViewModel
import com.example.routinetrack.ui.screens.settings.SettingsScreen
import com.example.routinetrack.ui.screens.settings.SettingsViewModel
import com.example.routinetrack.ui.screens.stats.StatsScreen
import com.example.routinetrack.ui.screens.stats.StatsViewModel

@Composable
fun RoutineTrackNavigation(
    currentThemeMode: AppThemeMode,
    onThemeModeChanged: (AppThemeMode) -> Unit,
    onLauncherIconApply: () -> Unit
) {
    val navController = rememberNavController()
    val app = LocalContext.current.applicationContext as RoutineTrackApplication
    val container = remember { app.container }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val loggedUser by container.authRepository.loggedUser.collectAsStateWithLifecycle(initialValue = null)
    val mainRoutes = setOf(
        Screen.Home.route,
        Screen.AddHabit.route,
        Screen.Stats.route,
        Screen.Settings.route
    )
    val authRoutes = setOf(Screen.Login.route, Screen.Register.route, Screen.PasswordReset.route)

    LaunchedEffect(loggedUser, currentRoute) {
        if (loggedUser != null && currentRoute in authRoutes) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
                launchSingleTop = true
            }
        }
        if (loggedUser == null && currentRoute != null && currentRoute !in authRoutes) {
            navController.navigate(Screen.Login.route) {
                popUpTo(0)
                launchSingleTop = true
            }
        }
    }

    // Scaffold contiene gli elementi comuni: bottom bar, FAB e area delle schermate.
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (currentRoute in mainRoutes) {
                BottomNavigationBar(navController = navController)
            }
        },
        floatingActionButton = { }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Login.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.Login.route) {
                    val authViewModel: AuthViewModel = viewModel(
                        factory = AuthViewModel.factory(container.authRepository)
                    )
                    LoginScreen(
                        viewModel = authViewModel,
                        onLoginSuccess = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        },
                        onRegisterClick = { navController.navigate(Screen.Register.route) },
                        onForgotPasswordClick = { navController.navigate(Screen.PasswordReset.route) }
                    )
                }
                composable(Screen.Register.route) {
                    val authViewModel: AuthViewModel = viewModel(
                        factory = AuthViewModel.factory(container.authRepository)
                    )
                    RegisterScreen(
                        viewModel = authViewModel,
                        onRegisterSuccess = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        },
                        onLoginClick = { navController.popBackStack() }
                    )
                }
                composable(Screen.PasswordReset.route) {
                    val authViewModel: AuthViewModel = viewModel(
                        factory = AuthViewModel.factory(container.authRepository)
                    )
                    PasswordResetScreen(
                        viewModel = authViewModel,
                        onLoginClick = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.PasswordReset.route) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable(Screen.Home.route) {
                    val homeViewModel: HomeViewModel = viewModel(
                        factory = HomeViewModel.factory(container.habitRepository)
                    )
                    HomeScreen(
                        viewModel = homeViewModel,
                        onHabitClick = { habitId ->
                            navController.navigate(Screen.HabitDetail.createRoute(habitId))
                        }
                    )
                }
                composable(
                    route = Screen.AddHabit.route,
                    arguments = listOf(
                        navArgument("habitId") {
                            type = NavType.LongType
                            defaultValue = -1L
                        }
                    )
                ) { entry ->
                    val habitId = entry.arguments?.getLong("habitId")?.takeIf { it > 0 }
                    val addHabitViewModel: AddHabitViewModel = viewModel(
                        key = "add_habit_${habitId ?: "new"}",
                        factory = AddHabitViewModel.factory(container.habitRepository, habitId)
                    )
                    AddHabitScreen(
                        viewModel = addHabitViewModel,
                        onBackClick = { navController.popBackStack() },
                        onSaved = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    )
                }
                composable(
                    route = Screen.HabitDetail.route,
                    arguments = listOf(navArgument("habitId") { type = NavType.LongType })
                ) { entry ->
                    val habitId = entry.arguments?.getLong("habitId") ?: return@composable
                    val detailViewModel: HabitDetailViewModel = viewModel(
                        key = "habit_detail_$habitId",
                        factory = HabitDetailViewModel.factory(
                            habitId = habitId,
                            habitRepository = container.habitRepository,
                            statsRepository = container.statsRepository
                        )
                    )
                    HabitDetailScreen(
                        viewModel = detailViewModel,
                        onBack = { navController.popBackStack() },
                        onEdit = { navController.navigate(Screen.AddHabit.createRoute(it)) }
                    )
                }
                composable(Screen.Calendar.route) {
                    val calendarViewModel: CalendarViewModel = viewModel(
                        factory = CalendarViewModel.factory(container.habitRepository)
                    )
                    CalendarScreen(
                        viewModel = calendarViewModel,
                        onHabitClick = { navController.navigate(Screen.HabitDetail.createRoute(it)) }
                    )
                }
                composable(Screen.Stats.route) {
                    val statsViewModel: StatsViewModel = viewModel(
                        factory = StatsViewModel.factory(container.statsRepository)
                    )
                    StatsScreen(viewModel = statsViewModel)
                }
                composable(Screen.Settings.route) {
                    val settingsViewModel: SettingsViewModel = viewModel(
                        factory = SettingsViewModel.factory(
                            authRepository = container.authRepository,
                            habitRepository = container.habitRepository,
                            syncRepository = container.syncRepository,
                            syncManager = container.syncManager,
                            profilePreferenceRepository = container.profilePreferenceRepository
                        )
                    )
                    SettingsScreen(
                        viewModel = settingsViewModel,
                        currentThemeMode = currentThemeMode,
                        onThemeModeChanged = onThemeModeChanged,
                        onLauncherIconApply = onLauncherIconApply,
                        onLoggedOut = {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0)
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    }
}
