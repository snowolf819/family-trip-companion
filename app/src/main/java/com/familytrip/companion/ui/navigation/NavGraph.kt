package com.familytrip.companion.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.familytrip.companion.ui.day.DayDetailScreen
import com.familytrip.companion.ui.home.HomeScreen
import com.familytrip.companion.ui.overview.TripOverviewScreen
import com.familytrip.companion.ui.packing.PackingScreen
import com.familytrip.companion.ui.settings.SettingsScreen
import com.familytrip.companion.viewmodel.SettingsViewModel
import com.familytrip.companion.viewmodel.TripViewModel

@Composable
fun NavGraph(deepLinkToken: String? = null) {
    val navController = rememberNavController()
    val tripVm: TripViewModel = viewModel()
    val settingsVm: SettingsViewModel = viewModel()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                viewModel = tripVm,
                onNavigateToTrip = { navController.navigate("trip") },
                onNavigateToSettings = { navController.navigate("settings") },
                deepLinkToken = deepLinkToken
            )
        }
        composable("trip") {
            TripOverviewScreen(
                viewModel = tripVm,
                onNavigateToDay = { dayIndex -> navController.navigate("day/$dayIndex") },
                onNavigateToPacking = { navController.navigate("packing") },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            "day/{dayIndex}",
            arguments = listOf(navArgument("dayIndex") { type = NavType.IntType })
        ) { entry ->
            val dayIndex = entry.arguments?.getInt("dayIndex") ?: 0
            DayDetailScreen(
                viewModel = tripVm,
                dayIndex = dayIndex,
                onBack = { navController.popBackStack() }
            )
        }
        composable("packing") {
            PackingScreen(
                viewModel = tripVm,
                onBack = { navController.popBackStack() }
            )
        }
        composable("settings") {
            SettingsScreen(
                viewModel = settingsVm,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
