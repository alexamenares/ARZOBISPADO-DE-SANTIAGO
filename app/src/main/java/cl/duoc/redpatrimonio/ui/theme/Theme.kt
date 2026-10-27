/*
 * Theme.kt aplica el estilo común a las pantallas Compose.
 * Combina la paleta de Color.kt, la tipografía de Type.kt y las formas redondeadas.
 * MainActivity envuelve AppNavigation con RedPatrimonioReligiosoTheme.
 * primary es color principal; onPrimary es el texto o icono que se dibuja encima.
 * Se usa un esquema claro fijo, sin selección automática de tema oscuro ni color dinámico.
 * El tema XML configura la ventana Android; este archivo configura los componentes Compose.
 */
package cl.duoc.redpatrimonio.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/*
 * El esquema asigna significado a cada color. Las vistas pueden pedir
 * MaterialTheme.colorScheme.primary sin repetir valores hexadecimales.
 */
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

/*
 * Recibe contenido Compose y le entrega colores, tipografía y esquinas compartidas.
 * No cambia datos ni navegación: solo proporciona el estilo visual.
 */
@Composable
fun RedPatrimonioReligiosoTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = PatrimonioColorScheme, typography = Typography,
        shapes = Shapes(extraSmall = RoundedCornerShape(8.dp), small = RoundedCornerShape(8.dp),
            medium = RoundedCornerShape(12.dp), large = RoundedCornerShape(16.dp), extraLarge = RoundedCornerShape(24.dp)),
        content = content)
}
