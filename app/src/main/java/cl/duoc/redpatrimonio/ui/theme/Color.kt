/*
 * Color.kt define la paleta visual de Compose: terracota, dorado y tonos neutros.
 * Color(0xFFAARRGGBB) usa hexadecimal: AA es opacidad; RR, GG y BB son rojo, verde y azul.
 * Theme.kt asigna estos colores a funciones como primary y background.
 * Algunas vistas también los utilizan directamente para conservar el diseño del prototipo.
 * Para cambiar un color compartido, se modifica su valor aquí.
 */
package cl.duoc.redpatrimonio.ui.theme

import androidx.compose.ui.graphics.Color

// Paleta del wireframe Mid-Fi corregido, recibido el 16-10-2026.
/*
 * Color principal de botones y acentos. Modificarlo aquí afecta a quienes lo referencian.
 */
val PatrimonioTerracota = Color(0xFFB85C38)
val PatrimonioDorado = Color(0xFFC9A227)
/*
 * Fondo general, superficie de tarjetas y colores de texto para mantener contraste.
 */
val PatrimonioFondo = Color(0xFFF5F1EA)
val PatrimonioSuperficie = Color(0xFFFFFFFF)
val PatrimonioTexto = Color(0xFF2B2B2B)
val PatrimonioSecundario = Color(0xFF6B6B6B)
val PatrimonioBorde = Color(0xFFE4DDD2)
val PatrimonioCrema = Color(0xFFF3E9CF)
val PatrimonioBlanco = Color(0xFFFFFFFF)
/*
 * Color usado por el tema para mensajes y campos con error.
 */
val PatrimonioError = Color(0xFFB3261E)
val PatrimonioVerde = Color(0xFF2D7552)
