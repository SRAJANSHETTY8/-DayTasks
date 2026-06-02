package com.example.todoapp.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PremiumDarkColorScheme = darkColorScheme(
    primary            = GoldPrimary,
    onPrimary          = DeepCharcoal,
    primaryContainer   = GoldSubtle,
    onPrimaryContainer = GoldLight,

    secondary          = GoldDim,
    onSecondary        = TextPrimary,
    secondaryContainer = SurfaceCard,
    onSecondaryContainer = TextSecondary,

    background         = DeepCharcoal,
    onBackground       = TextPrimary,

    surface            = SurfaceDark,
    onSurface          = TextPrimary,
    surfaceVariant     = SurfaceElevated,
    onSurfaceVariant   = TextSecondary,

    outline            = SurfaceBorder,
    outlineVariant     = Color(0xFF252528),

    error              = DangerLight,
    onError            = DeepCharcoal,

    inverseSurface     = TextPrimary,
    inverseOnSurface   = DeepCharcoal,
    inversePrimary     = GoldDim,
)

@Composable
fun TodoAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PremiumDarkColorScheme,
        typography  = PremiumTypography,
        content     = content
    )
}