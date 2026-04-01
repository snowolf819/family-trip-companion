package com.trip.family

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.trip.family.ui.screens.extractShareToken
import com.trip.family.ui.theme.FamilyTripTheme
import kotlinx.coroutines.flow.MutableSharedFlow

class MainActivity : ComponentActivity() {

    private val shareTokenFlow = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 1
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            val prefs = remember(context) { com.trip.family.TripPreferences(context) }
            val currentFontScale = remember { mutableFloatStateOf(prefs.fontScale) }
            FamilyTripTheme(fontScale = currentFontScale.floatValue) {
                AppNavigation(
                    shareTokenFlow = shareTokenFlow,
                    fontScaleState = currentFontScale,
                    prefs = prefs
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val token = extractShareToken(intent)
        if (token != null) {
            shareTokenFlow.tryEmit(token)
        }
    }
}

enum class Screen { Home, Overview, DayDetail, Settings }

@Composable
fun AppNavigation(
    shareTokenFlow: MutableSharedFlow<String>,
    fontScaleState: MutableFloatState,
    prefs: com.trip.family.TripPreferences
) {
    var currentScreen by remember { mutableStateOf(Screen.Home) }
    var selectedDayNumber by remember { mutableIntStateOf(0) }

    // 系统返回键处理
    BackHandler(enabled = currentScreen != Screen.Home) {
        currentScreen = when (currentScreen) {
            Screen.DayDetail -> Screen.Overview
            Screen.Overview, Screen.Settings -> Screen.Home
            Screen.Home -> Screen.Home
        }
    }

    when (currentScreen) {
        Screen.Home -> com.trip.family.ui.screens.HomeScreen(
            shareTokenFlow = shareTokenFlow,
            onTripLoaded = { currentScreen = Screen.Overview },
            onOpenSettings = { currentScreen = Screen.Settings }
        )
        Screen.Overview -> com.trip.family.ui.screens.TripOverviewScreen(
            onDayClick = { dayNum ->
                selectedDayNumber = dayNum
                currentScreen = Screen.DayDetail
            },
            onBack = { currentScreen = Screen.Home },
            onOpenSettings = { currentScreen = Screen.Settings }
        )
        Screen.DayDetail -> com.trip.family.ui.screens.DayDetailScreen(
            dayNumber = selectedDayNumber,
            onBack = { currentScreen = Screen.Overview }
        )
        Screen.Settings -> com.trip.family.ui.screens.SettingsScreen(
            fontScaleState = fontScaleState,
            prefs = prefs,
            onBack = { currentScreen = Screen.Home },
            onSaved = { currentScreen = Screen.Home }
        )
    }
}
