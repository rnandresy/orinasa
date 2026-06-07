package com.orinasa.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val LightColorScheme = lightColorScheme(
    primary              = Primary,
    onPrimary            = White,
    primaryContainer     = Color(0xFFE8EAF6),
    onPrimaryContainer   = Primary,
    secondary            = Secondary,
    onSecondary          = White,
    secondaryContainer   = Color(0xFFE3F2FD),
    onSecondaryContainer = Secondary,
    tertiary             = Accent,
    onTertiary           = White,
    tertiaryContainer    = Color(0xFFE0F7FA),
    onTertiaryContainer  = Color(0xFF006064),
    background           = Surface0,
    onBackground         = TextPri,
    surface              = White,
    onSurface            = TextPri,
    surfaceVariant       = Surface1,
    onSurfaceVariant     = TextSec,
    outline              = Border,
    outlineVariant       = Surface2,
    error                = Color(0xFFDC2626),
    onError              = White,
    errorContainer       = Color(0xFFFEE2E2),
    onErrorContainer     = Color(0xFF991B1B),
    inverseSurface       = Primary,
    inverseOnSurface     = White,
    inversePrimary       = Accent,
    scrim                = Color(0x80000000)
)

val OrinasaTypography = Typography(
    displayLarge  = TextStyle(fontWeight = FontWeight.Black,     fontSize = 32.sp, letterSpacing = (-0.5).sp),
    displayMedium = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 26.sp),
    displaySmall  = TextStyle(fontWeight = FontWeight.Bold,      fontSize = 22.sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold,      fontSize = 20.sp),
    headlineMedium= TextStyle(fontWeight = FontWeight.SemiBold,  fontSize = 18.sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.SemiBold,  fontSize = 16.sp),
    titleLarge    = TextStyle(fontWeight = FontWeight.Bold,      fontSize = 18.sp),
    titleMedium   = TextStyle(fontWeight = FontWeight.SemiBold,  fontSize = 16.sp),
    titleSmall    = TextStyle(fontWeight = FontWeight.SemiBold,  fontSize = 14.sp),
    bodyLarge     = TextStyle(fontWeight = FontWeight.Normal,    fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium    = TextStyle(fontWeight = FontWeight.Normal,    fontSize = 14.sp, lineHeight = 22.sp),
    bodySmall     = TextStyle(fontWeight = FontWeight.Normal,    fontSize = 12.sp, lineHeight = 18.sp),
    labelLarge    = TextStyle(fontWeight = FontWeight.SemiBold,  fontSize = 14.sp),
    labelMedium   = TextStyle(fontWeight = FontWeight.Medium,    fontSize = 12.sp),
    labelSmall    = TextStyle(fontWeight = FontWeight.Medium,    fontSize = 11.sp)
)

@Composable
fun OrinasaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography  = OrinasaTypography,
        content     = content
    )
}