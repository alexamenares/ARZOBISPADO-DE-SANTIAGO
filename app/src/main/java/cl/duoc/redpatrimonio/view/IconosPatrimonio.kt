/*
 * IconosPatrimonio.kt dibuja los iconos de la app con Canvas.
 * Simbolo enumera los dibujos e Icono elige uno con when.
 * Son trazos y figuras locales: por eso no se necesita material-icons-extended.
 * La descripción se expone a servicios de accesibilidad cuando el icono comunica una acción.
 * Para explicarlo: este archivo aporta dibujos reutilizables, no lógica de navegación.
 */
package cl.duoc.redpatrimonio.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/*
 * Lista cerrada de iconos que las pantallas pueden solicitar por nombre.
 */
enum class Simbolo { INICIO, BUSCAR, MAPA, RUTA, CALENDARIO, PERFIL, FAVORITO, ATRAS, MAS,
    CAMPANA, EDIFICIO, RELOJ, CHECK, CORREO, CANDADO, FILTRO, LISTA, FLECHA, MENU }

/** Iconos vectoriales locales: no requieren una biblioteca de iconos adicional. */
@Composable
fun Icono(simbolo: Simbolo, descripcion: String? = null, modifier: Modifier = Modifier,
    color: Color = LocalContentColor.current) {
    Canvas(modifier.size(24.dp).then(if (descripcion == null) Modifier else Modifier.semantics { contentDescription = descripcion })) {
        /*
         * Normaliza los dibujos a una cuadrícula de 24 por 24 y los escala al tamaño disponible.
         */
        withTransform({ scale(size.width / 24f, size.height / 24f, Offset.Zero) }) {
            val trazo = Stroke(width = 1.8f, cap = StrokeCap.Round)
            /*
             * Ayudas locales de dibujo: linea une dos puntos y camino une varios segmentos.
             * Solo se usan dentro del Canvas de Icono.
             */
            fun linea(x: Float, y: Float, x2: Float, y2: Float) = drawLine(color, Offset(x,y), Offset(x2,y2), 1.8f, StrokeCap.Round)
            fun camino(vararg puntos: Float) {
                val p = Path(); p.moveTo(puntos[0], puntos[1])
                for (i in 2 until puntos.size step 2) p.lineTo(puntos[i], puntos[i+1])
                drawPath(p, color, style = trazo)
            }
            /*
             * Cada opción dibuja su propia figura. La descripción accesible viene del llamador.
             */
            when (simbolo) {
                Simbolo.INICIO -> { camino(3f,11f,12f,3f,21f,11f); camino(5f,10f,5f,21f,10f,21f,10f,15f,14f,15f,14f,21f,19f,21f,19f,10f) }
                Simbolo.BUSCAR -> { drawCircle(color,6.5f,Offset(10f,10f),style=trazo);linea(15f,15f,21f,21f) }
                Simbolo.MAPA -> { camino(3f,5f,9f,3f,15f,6f,21f,3f,21f,19f,15f,22f,9f,19f,3f,21f,3f,5f);linea(9f,3f,9f,19f);linea(15f,6f,15f,22f) }
                Simbolo.RUTA -> { drawCircle(color,2.5f,Offset(5f,5f),style=trazo);drawCircle(color,2.5f,Offset(19f,19f),style=trazo);camino(5f,8f,5f,15f,13f,15f,13f,5f,20f,5f) }
                Simbolo.CALENDARIO -> { drawRoundRect(color,Offset(3f,5f),Size(18f,16f),CornerRadius(2f),style=trazo);linea(7f,3f,7f,7f);linea(17f,3f,17f,7f);linea(3f,10f,21f,10f);linea(7f,14f,9f,14f);linea(14f,14f,16f,14f);linea(7f,18f,9f,18f) }
                Simbolo.PERFIL -> { drawCircle(color,4f,Offset(12f,7f),style=trazo);drawPath(Path().apply { moveTo(4f,21f);cubicTo(4f,11f,20f,11f,20f,21f);close() },color,style=trazo) }
                Simbolo.FAVORITO -> drawPath(Path().apply { moveTo(12f,21f);cubicTo(-5f,10f,5f,-3f,12f,6f);cubicTo(19f,-3f,29f,10f,12f,21f);close() },color,style=trazo)
                Simbolo.ATRAS -> { camino(13f,4f,5f,12f,13f,20f);linea(5f,12f,21f,12f) }
                Simbolo.FLECHA -> camino(9f,5f,16f,12f,9f,19f)
                Simbolo.MAS -> { linea(12f,4f,12f,20f);linea(4f,12f,20f,12f) }
                Simbolo.CHECK -> camino(4f,12f,9f,18f,21f,5f)
                Simbolo.CAMPANA -> { camino(4f,17f,6f,14f,6f,9f,8f,5f,12f,3f,16f,5f,18f,9f,18f,14f,20f,17f,4f,17f);linea(10f,21f,14f,21f) }
                Simbolo.EDIFICIO -> { camino(3f,8f,12f,2f,21f,8f,3f,8f);for (x in listOf(6f,12f,18f)) linea(x,11f,x,19f);linea(3f,22f,21f,22f) }
                Simbolo.RELOJ -> { drawCircle(color,9f,Offset(12f,12f),style=trazo);camino(12f,6f,12f,12f,16f,14f) }
                Simbolo.CORREO -> { drawRoundRect(color,Offset(2f,4f),Size(20f,16f),CornerRadius(2f),style=trazo);camino(3f,6f,12f,13f,21f,6f) }
                Simbolo.CANDADO -> { drawRoundRect(color,Offset(4f,10f),Size(16f,12f),CornerRadius(2f),style=trazo);drawPath(Path().apply{moveTo(8f,10f);lineTo(8f,6f);cubicTo(8f,0f,16f,0f,16f,6f);lineTo(16f,10f)},color,style=trazo);linea(12f,15f,12f,18f) }
                Simbolo.FILTRO -> { camino(3f,4f,21f,4f,14f,12f,14f,20f,10f,22f,10f,12f,3f,4f) }
                Simbolo.LISTA, Simbolo.MENU -> { for(y in listOf(6f,12f,18f)) {linea(8f,y,21f,y);drawCircle(color,1f,Offset(3f,y))} }
            }
        }
    }
}
