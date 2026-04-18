package com.familytrip.companion.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.familytrip.companion.ui.day.DayDetailScreen
import com.familytrip.companion.ui.home.HomeScreen
import com.familytrip.companion.ui.overview.TripOverviewScreen
import com.familytrip.companion.ui.packing.PackingScreen
import com.familytrip.companion.ui.settings.SettingsScreen
import com.familytrip.companion.viewmodel.SettingsViewModel
import com.familytrip.companion.viewmodel.TripViewModel

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val tripVM: TripViewModel = viewModel()
    val settingsVM: SettingsViewModel = viewModel()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                viewModel = tripVM,
                onNavigateToTrip = { navController.navigate("overview") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("overview") {
            TripOverviewScreen(
                viewModel = tripVM,
                onNavigateToDay = { navController.navigate("day") },
                onNavigateToPacking = { navController.navigate("packing") },
                onBack = { navController.popBackStack() }
            )
        }
        composable("day") {
            DayDetailScreen(viewModel = tripVM, onBack = { navController.popBackStack() })
        }
        composable("packing") {
            PackingScreen(viewModel = tripVM, onBack = { navController.popBackStack() })
        }
        composable("settings") {
            SettingsScreen(viewModel = settingsVM, onBack = { navController.popBackStack() })
        }
    }
}
