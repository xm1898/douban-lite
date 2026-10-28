package app.doubanlite.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

private val DoubanGreen = Color(0xFF00A82D)
private val DoubanGreenDark = Color(0xFF6BD68A)
private val DoubanOrange = Color(0xFFF5A623)
private val DoubanOrangeDark = Color(0xFFFFB94D)

private val LightColors = lightColorScheme(
    primary = DoubanGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD8F7E0),
    onPrimaryContainer = Color(0xFF00390F),
    secondary = DoubanOrange,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFEBC2),
    onSecondaryContainer = Color(0xFF3D2A00),
    background = Color(0xFFF7F8FA),
    onBackground = Color(0xFF141414),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF141414),
    surfaceVariant = Color(0xFFF0F1F3),
    onSurfaceVariant = Color(0xFF5F6368),
    surfaceContainer = Color(0xFFFFFFFF),
    surfaceContainerHigh = Color(0xFFF1F2F5),
    surfaceContainerHighest = Color(0xFFEAEBEF),
    outline = Color(0xFFC7C9CF),
    outlineVariant = Color(0xFFE3E4E8),
)

private val DarkColors = darkColorScheme(
    primary = DoubanGreenDark,
    onPrimary = Color(0xFF00390F),
    primaryContainer = Color(0xFF00531A),
    onPrimaryContainer = Color(0xFF9DF3B4),
    secondary = DoubanOrangeDark,
    onSecondary = Color(0xFF2A1A00),
    secondaryContainer = Color(0xFF4A3300),
    onSecondaryContainer = Color(0xFFFFEBC2),
    background = Color(0xFF0F1012),
    onBackground = Color(0xFFECECEC),
    surface = Color(0xFF16171A),
    onSurface = Color(0xFFECECEC),
    surfaceVariant = Color(0xFF1E2024),
    onSurfaceVariant = Color(0xFFA6A9AE),
    surfaceContainer = Color(0xFF16171A),
    surfaceContainerHigh = Color(0xFF1E2024),
    surfaceContainerHighest = Color(0xFF26282D),
    outline = Color(0xFF3A3D43),
    outlineVariant = Color(0xFF2C2F35),
)

private val DoubanTypography = Typography(
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
    ),
)

private val DoubanShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

@Composable
fun DoubanLiteTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        val context = LocalContext.current
        SideEffect {
            val window = (context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colors,
        typography = DoubanTypography,
        shapes = DoubanShapes,
        content = content,
    )
}