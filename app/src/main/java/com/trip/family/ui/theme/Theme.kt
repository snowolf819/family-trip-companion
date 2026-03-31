package com.trip.family.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// 适老化配色：高对比度、温暖色调
private val PrimaryOrange = Color(0xFFE65100)
private val PrimaryOrangeLight = Color(0xFFFF8A50)
private val WarmGray = Color(0xFFF5F5F0)

@Composable
fun FamilyTripTheme(content: @Composable () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = PrimaryOrangeLight,
            onPrimary = Color.White,
            secondary = Color(0xFF78909C),
            surface = Color(0xFF1E1E1E),
            onSurface = Color(0xFFE0E0E0),
            onSurfaceVariant = Color(0xFFBDBDBD),
            errorContainer = Color(0xFF5C1A1A),
            onErrorContainer = Color(0xFFFFB4AB),
            tertiaryContainer = Color(0xFF1A3333),
            secondaryContainer = Color(0xFF332E00),
        )
    } else {
        lightColorScheme(
            primary = PrimaryOrange,
            onPrimary = Color.White,
            primaryContainer = Color(0xFFFFE0B2),
            secondary = Color(0xFF5D4037),
            surface = WarmGray,
            onSurface = Color(0xFF212121),
            onSurfaceVariant = Color(0xFF616161),
            surfaceVariant = Color(0xFFEEEEEE),
            errorContainer = Color(0xFFFFEBEE),
            onErrorContainer = Color(0xFFB3261E),
            tertiaryContainer = Color(0xFFFFF8E1),
            secondaryContainer = Color(0xFFFFF3E0),
        )
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = elderlyTypography(),
        content = content
    )
}

/**
 * 适老化字体：整体放大，行高增加，提升阅读体验
 */
fun elderlyTypography(): Typography {
    return Typography(
        displayLarge = TextStyle(fontSize = 60.sp, fontWeight = FontWeight.W400),
        headlineLarge = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.W400),
        headlineMedium = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.W400),
        titleLarge = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.W500),
        titleMedium = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.W500),
        bodyLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.W400, lineHeight = 32.sp),
        bodyMedium = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.W400, lineHeight = 28.sp),
        labelLarge = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.W500),
        labelMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.W500),
    )
}
