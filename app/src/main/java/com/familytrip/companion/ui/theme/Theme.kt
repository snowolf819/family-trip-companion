package com.familytrip.companion.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import com.familytrip.companion.data.local.PreferencesManager

@Composable
fun FamilyTripCompanionTheme(
    prefsManager: PreferencesManager? = null,
    content: @Composable () -> Unit
) {
    val darkMode by prefsManager?.darkMode?.collectAsState(initial = false) ?: remember { mutableStateOf(false) }

    val colorScheme = if (darkMode == true || isSystemInDarkTheme()) {
        darkColorScheme(primary = Primary, secondary = Secondary)
    } else {
        lightColorScheme(
            primary = Primary, onPrimary = OnPrimary,
            secondary = Secondary, onSecondary = OnSecondary,
            background = Background, onBackground = OnBackground,
            surface = Surface, onSurface = OnSurface,
            error = Error
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = FamilyTripTypography,
        content = content
    )
}
