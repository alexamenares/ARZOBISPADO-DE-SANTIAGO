/*
 * PatrimonioEmblem.kt dibuja el emblema y las ilustraciones de recintos.
 * Usa Canvas y Path para crear figuras vectoriales con líneas, curvas y colores locales.
 * Las dimensiones se ajustan al espacio del componente. variante alterna el tono de la ilustración.
 * Son dibujos del prototipo, no fotografías ni imágenes descargadas de los recintos reales.
 */
package cl.duoc.redpatrimonio.view

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import cl.duoc.redpatrimonio.ui.theme.*

/*
 * Dibuja un escudo con una cruz y lo centra en el tamaño disponible.
 */
@Composable
internal fun PatrimonioEmblem(modifier: Modifier = Modifier, color: Color = PatrimonioDorado) {
    Canvas(modifier) {
        val lado = minOf(size.width, size.height)
        withTransform({ translate((size.width-lado)/2, (size.height-lado)/2); scale(lado/100, lado/100, Offset.Zero) }) {
            drawPath(Path().apply { moveTo(50f,5f);lineTo(82f,20f);lineTo(82f,60f);quadraticBezierTo(80f,80f,50f,96f);quadraticBezierTo(20f,80f,18f,60f);lineTo(18f,20f);close() }, color, style=Stroke(3f))
            drawLine(color,Offset(50f,23f),Offset(50f,75f),4f)
            drawLine(color,Offset(32f,42f),Offset(68f,42f),4f)
        }
    }
}

/** Ilustración vectorial del prototipo, no fotografía del recinto. */
@Composable
internal fun IlustracionRecinto(modifier: Modifier = Modifier, variante: Int = 0) {
    Canvas(modifier) {
        /*
         * El ID o variante solo elige un tono de la ilustración; no identifica una fotografía.
         */
        val tono = if (variante % 2 == 0) PatrimonioDorado else PatrimonioTerracota
        drawRect(Brush.verticalGradient(listOf(tono.copy(alpha=.12f),tono.copy(alpha=.32f))))
        val lado = minOf(size.width * .8f, size.height * .82f)
        withTransform({ translate((size.width-lado)/2, size.height-lado); scale(lado/100,lado/100,Offset.Zero) }) {
            val tinta = tono.copy(alpha=.65f)
            val p = Path().apply {
                moveTo(8f,100f);lineTo(8f,38f);lineTo(18f,26f);lineTo(28f,38f);lineTo(28f,100f)
                moveTo(72f,100f);lineTo(72f,38f);lineTo(82f,26f);lineTo(92f,38f);lineTo(92f,100f)
                moveTo(28f,100f);lineTo(28f,58f);lineTo(50f,40f);lineTo(72f,58f);lineTo(72f,100f)
                moveTo(42f,100f);lineTo(42f,82f);quadraticBezierTo(50f,68f,58f,82f);lineTo(58f,100f)
            }
            drawPath(p,tinta,style=Stroke(1.5f))
            drawLine(tinta,Offset(50f,20f),Offset(50f,39f),1.5f)
            drawLine(tinta,Offset(44f,27f),Offset(56f,27f),1.5f)
            drawLine(tinta,Offset(4f,99f),Offset(96f,99f),2f)
            drawCircle(tinta,4f,Offset(50f,60f),style=Stroke(1.5f))
            for(x in listOf(15f,79f)) for(y in listOf(48f,65f,82f)) drawRect(tinta,Offset(x,y),Size(6f,8f),style=Stroke(1f))
        }
    }
}
