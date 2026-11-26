package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = RaspberryPrimary,
    onPrimary = PureWhite,
    primaryContainer = RaspberryLight,
    onPrimaryContainer = RaspberryPrimary,
    secondary = RaspberrySecondary,
    onSecondary = PureWhite,
    secondaryContainer = GoldCoinLight,
    onSecondaryContainer = GoldCoinDeep,
    tertiary = BlueStar,
    onTertiary = PureWhite,
    tertiaryContainer = BlueStarLight,
    onTertiaryContainer = BlueStar,
    background = PureWhite,
    onBackground = TextPrimary,
    surface = PureWhite,
    onSurface = TextPrimary,
    surfaceVariant = SoftCardBg,
    onSurfaceVariant = TextSecondary,
    outline = SubtleBorder,
    outlineVariant = SubtleBorder,
    error = ErrorRed,
    onError = PureWhite
)

private val DarkColorScheme = darkColorScheme(
    primary = RaspberrySecondary,
    onPrimary = PureWhite,
    primaryContainer = RaspberryDeep,
    onPrimaryContainer = RaspberryLight,
    secondary = GoldCoin,
    onSecondary = TextPrimary,
    secondaryContainer = DarkCard,
    onSecondaryContainer = GoldCoin,
    tertiary = BlueStar,
    onTertiary = PureWhite,
    tertiaryContainer = DarkCard,
    onTertiaryContainer = BlueStarLight,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkCard,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    outlineVariant = DarkBorder,
    error = ErrorRed,
    onError = PureWhite
)

val CreatorBoostShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun CreatorBoostTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = CreatorBoostShapes,
        content = content
    )
}
