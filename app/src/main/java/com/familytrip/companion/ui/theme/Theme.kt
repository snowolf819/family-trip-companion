package com.familytrip.companion.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.familytrip.companion.data.local.PreferencesManager

@Composable
fun FamilyTripCompanionTheme(
    prefsManager: PreferencesManager? = null,
    content: @Composable () -> Unit
) {
    val darkMode by prefsManager?.darkMode?.collectAsState(initial = false) ?: remember { mutableStateOf(false) }
    val fontScale by prefsManager?.fontScale?.collectAsState(initial = 1.0f) ?: remember { mutableStateOf(1.0f) }

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

    val scaledTypography = remember(fontScale) {
        FamilyTripTypography.copy(
            displayLarge = FamilyTripTypography.displayLarge.copy(fontSize = 32.sp * fontScale),
            displayMedium = FamilyTripTypography.displayMedium.copy(fontSize = 28.sp * fontScale),
            headlineLarge = FamilyTripTypography.headlineLarge.copy(fontSize = 26.sp * fontScale),
            headlineMedium = FamilyTripTypography.headlineMedium.copy(fontSize = 24.sp * fontScale),
            headlineSmall = FamilyTripTypography.headlineSmall.copy(fontSize = 22.sp * fontScale),
            titleLarge = FamilyTripTypography.titleLarge.copy(fontSize = 22.sp * fontScale),
            titleMedium = FamilyTripTypography.titleMedium.copy(fontSize = 18.sp * fontScale),
            titleSmall = FamilyTripTypography.titleSmall.copy(fontSize = 16.sp * fontScale),
            bodyLarge = FamilyTripTypography.bodyLarge.copy(fontSize = 18.sp * fontScale),
            bodyMedium = FamilyTripTypography.bodyMedium.copy(fontSize = 16.sp * fontScale),
            bodySmall = FamilyTripTypography.bodySmall.copy(fontSize = 14.sp * fontScale),
            labelLarge = FamilyTripTypography.labelLarge.copy(fontSize = 16.sp * fontScale),
            labelMedium = FamilyTripTypography.labelMedium.copy(fontSize = 14.sp * fontScale),
            labelSmall = FamilyTripTypography.labelSmall.copy(fontSize = 12.sp * fontScale)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = scaledTypography,
        content = content
    )
}
