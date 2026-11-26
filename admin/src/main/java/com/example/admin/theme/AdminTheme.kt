package com.example.admin.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val AdminPrimary = Color(0xFFD10F46)
val AdminPrimaryLight = Color(0xFFFDEBF0)
val AdminPrimaryDark = Color(0xFF9E0B35)
val AdminGold = Color(0xFFF6BE35)
val AdminGoldBg = Color(0xFFFFF8E1)
val AdminBlue = Color(0xFF2563EB)
val AdminBlueBg = Color(0xFFEBF2FE)
val AdminGreen = Color(0xFF16A34A)
val AdminGreenBg = Color(0xFFE6F8ED)
val AdminWarmCard = Color(0xFFF7F5EE)
val AdminSurfaceSoft = Color(0xFFF6F6F8)
val AdminBorder = Color(0xFFEAEAEF)
val AdminTextPrimary = Color(0xFF141418)
val AdminTextSecondary = Color(0xFF6B7280)
val AdminDanger = Color(0xFFDC2626)
val AdminDangerBg = Color(0xFFFEE2E2)

private val AdminColorScheme = lightColorScheme(
    primary = AdminPrimary,
    onPrimary = Color.White,
    primaryContainer = AdminPrimaryLight,
    onPrimaryContainer = AdminPrimaryDark,
    secondary = AdminGold,
    onSecondary = AdminTextPrimary,
    background = Color(0xFFFAFAFC),
    onBackground = AdminTextPrimary,
    surface = Color.White,
    onSurface = AdminTextPrimary,
    surfaceVariant = AdminSurfaceSoft,
    onSurfaceVariant = AdminTextSecondary,
    outline = AdminBorder,
    error = AdminDanger
)

private val AdminTypography = Typography(
    headlineMedium = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AdminTextPrimary),
    headlineSmall = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AdminTextPrimary),
    titleLarge = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AdminTextPrimary),
    titleMedium = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = AdminTextPrimary),
    titleSmall = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AdminTextPrimary),
    bodyLarge = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Normal, color = AdminTextPrimary),
    bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal, color = AdminTextPrimary),
    bodySmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal, color = AdminTextSecondary),
    labelLarge = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
    labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium)
)

@Composable
fun AdminTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AdminColorScheme,
        typography = AdminTypography,
        content = content
    )
}
