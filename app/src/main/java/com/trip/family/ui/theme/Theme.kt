package com.trip.family.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// 适老化配色：高对比度、温暖色调
private val PrimaryOrange = Color(0xFFE65100)
private val PrimaryOrangeLight = Color(0xFFFF8A50)
private val WarmGray = Color(0xFFF5F5F0)

/**
 * 全局字体缩放比例，通过 CompositionLocal 传递
 */
val LocalFontScale = staticCompositionLocalOf { 1.0f }

@Composable
fun FamilyTripTheme(
    fontScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
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

    CompositionLocalProvider(LocalFontScale provides fontScale) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = elderlyTypography(fontScale),
            content = content
        )
    }
}

/**
 * 适老化字体：整体放大，行高增加，提升阅读体验
 * @param scale 字体缩放比例，1.0 为标准大小
 */
fun elderlyTypography(scale: Float = 1.0f): Typography {
    val s = { base: Int -> (base * scale).sp }
    val lh = { base: Float -> (base * scale).sp }

    return Typography(
        displayLarge = TextStyle(fontSize = s(60), fontWeight = FontWeight.W400),
        headlineLarge = TextStyle(fontSize = s(36), fontWeight = FontWeight.W400),
        headlineMedium = TextStyle(fontSize = s(30), fontWeight = FontWeight.W400),
        titleLarge = TextStyle(fontSize = s(26), fontWeight = FontWeight.W500),
        titleMedium = TextStyle(fontSize = s(22), fontWeight = FontWeight.W500),
        bodyLarge = TextStyle(fontSize = s(20), fontWeight = FontWeight.W400, lineHeight = lh(32f)),
        bodyMedium = TextStyle(fontSize = s(18), fontWeight = FontWeight.W400, lineHeight = lh(28f)),
        labelLarge = TextStyle(fontSize = s(18), fontWeight = FontWeight.W500),
        labelMedium = TextStyle(fontSize = s(16), fontWeight = FontWeight.W500),
    )
}
