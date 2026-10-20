package cl.duoc.redpatrimonio.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val PatrimonioColorScheme = lightColorScheme(
    primary = PatrimonioTerracota, onPrimary = PatrimonioBlanco,
    primaryContainer = Color(0xFFF6E4DC), onPrimaryContainer = Color(0xFF773B24),
    secondary = PatrimonioDorado, onSecondary = PatrimonioTexto,
    secondaryContainer = PatrimonioCrema, onSecondaryContainer = Color(0xFF655019),
    background = PatrimonioFondo, onBackground = PatrimonioTexto,
    surface = PatrimonioSuperficie, onSurface = PatrimonioTexto,
    surfaceVariant = PatrimonioFondo, onSurfaceVariant = PatrimonioSecundario,
    outline = PatrimonioSecundario, outlineVariant = PatrimonioBorde, error = PatrimonioError
)

@Composable
fun RedPatrimonioReligiosoTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = PatrimonioColorScheme, typography = Typography,
        shapes = Shapes(extraSmall = RoundedCornerShape(8.dp), small = RoundedCornerShape(8.dp),
            medium = RoundedCornerShape(12.dp), large = RoundedCornerShape(16.dp), extraLarge = RoundedCornerShape(24.dp)),
        content = content)
}
