/*
 * QrScannerScreen.kt lee QR con Google Code Scanner, siguiendo el enfoque de clases.
 * Al pulsar Escanear QR, Google Play Services abre su interfaz y devuelve el texto leído.
 * La app no controla CameraX ni solicita el permiso CAMERA para esta función.
 * El texto debe tener formato recinto:ID. Se extrae el número y se llama a onRecintoDetectado.
 * AppNavigation comprueba el recinto y abre la ficha. También se muestran formato inválido,
 * recinto inexistente, cancelación y fallo del escáner. El módulo de Google debe estar disponible.
 * Para explicarlo: Google lee el código; nuestra app decide qué significa y adónde navegar.
 */
package cl.duoc.redpatrimonio.view

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

/**
 * Define el formato válido de un QR asociado a un recinto.
 *
 * Ejemplos válidos:
 * - recinto:1
 * - RECINTO: 2
 */
private val patronQrRecinto = Regex(
    pattern = "^recinto\\s*:\\s*(\\d+)$",
    option = RegexOption.IGNORE_CASE
)

/**
 * Pantalla para leer códigos QR mediante Google Code Scanner.
 *
 * Google Play Services abre su propia interfaz de cámara, por lo que
 * esta aplicación no necesita controlar CameraX directamente.
 *
 * El QR debe contener el formato "recinto:ID". Si el identificador existe,
 * [onRecintoDetectado] navega hacia la ficha patrimonial correspondiente.
 */
@Composable
fun QrScannerScreen(
    onRecintoDetectado: (Int) -> Boolean
) {
    val context = LocalContext.current

    /*
     * Estado temporal de esta pantalla. procesandoQr evita abrir varios escaneos
     * por pulsaciones consecutivas; al fallar o cancelar vuelve a false.
     */
    var mensaje by remember { mutableStateOf<String?>(null) }
    var procesandoQr by remember { mutableStateOf(false) }

    /*
     * El scanner se configura para reconocer únicamente códigos QR.
     * Esto evita procesar otros formatos de códigos de barras.
     */
    val opciones = remember {
        GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    }

    /*
     * Google Play Services administra la interfaz de cámara y devuelve
     * el contenido leído cuando el usuario escanea un código válido.
     */
    val scanner = remember(context, opciones) {
        GmsBarcodeScanning.getClient(context, opciones)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Escanear código QR",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Presiona el botón y apunta la cámara al código QR del recinto. " +
                    "El formato esperado es recinto:ID.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (!procesandoQr) {
                    procesandoQr = true
                    mensaje = "Abriendo cámara..."

                    /*
                     * Operación asíncrona: los listeners reciben éxito, cancelación o fallo más tarde.
                     * No se bloquea la pantalla esperando que la persona encuadre el QR.
                     */
                    scanner.startScan()
                        .addOnSuccessListener { barcode ->
                            /*
                             * rawValue contiene el texto original. orEmpty evita trabajar con un valor nulo.
                             */
                            val contenido = barcode.rawValue.orEmpty()

                            /*
                             * La expresión regular exige recinto: seguido de dígitos y admite mayúsculas o espacios.
                             * El grupo 1 contiene los dígitos; toIntOrNull evita fallar con números fuera del rango Int.
                             */
                            val coincidencia = patronQrRecinto.matchEntire(
                                contenido.trim()
                            )

                            val recintoId = coincidencia
                                ?.groupValues
                                ?.getOrNull(1)
                                ?.toIntOrNull()

                            when {
                                recintoId == null -> {
                                    mensaje = "El QR no tiene el formato esperado. " +
                                            "Usa, por ejemplo, recinto:1."
                                    procesandoQr = false
                                }

                                /*
                                 * La pantalla no conoce el NavController. Delega al callback registrado en AppNavigation.
                                 * Si devuelve true se abre la ficha y se retira el escáner de la pila de navegación.
                                 */
                                onRecintoDetectado(recintoId) -> {
                                    mensaje = "Recinto encontrado. Abriendo ficha..."
                                }

                                else -> {
                                    mensaje = "El recinto con id $recintoId no existe " +
                                            "en la aplicación."
                                    procesandoQr = false
                                }
                            }
                        }
                        /*
                         * Cancelar es una decisión del usuario; vuelve a habilitar un intento sin cerrar la app.
                         */
                        .addOnCanceledListener {
                            mensaje = "Escaneo cancelado."
                            procesandoQr = false
                        }
                        /*
                         * Fallo técnico: muestra un aviso y un Toast breve. Puede ocurrir, por ejemplo,
                         * si el módulo de Google todavía no está disponible en ese dispositivo.
                         */
                        .addOnFailureListener { error ->
                            mensaje = "No fue posible leer el código QR."
                            procesandoQr = false

                            Toast.makeText(
                                context,
                                "Error: ${error.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (procesandoQr) {
                    "Procesando..."
                } else {
                    "Escanear QR"
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        mensaje?.let { texto ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = texto,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
