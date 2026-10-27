/*
 * MainActivity.kt es la puerta de entrada de la aplicación.
 * Android crea esta actividad porque está declarada en AndroidManifest.xml.
 * onCreate prepara la ventana; setContent inicia la interfaz Jetpack Compose.
 * El tema aplica colores y fuentes. AppNavigation decide qué pantalla se muestra.
 * Para explicarlo: aquí arranca la interfaz, no se validan usuarios ni se consulta Room.
 */
package cl.duoc.redpatrimonio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cl.duoc.redpatrimonio.ui.theme.RedPatrimonioReligiosoTheme
import cl.duoc.redpatrimonio.navigation.AppNavigation

class MainActivity : ComponentActivity() {

    /*
     * Android llama a onCreate al crear la actividad. Bundle puede traer estado previo.
     * Primero se ejecuta la preparación de la clase base con super.onCreate.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*
         * Permite dibujar detrás de las barras del sistema. Las vistas aplican los márgenes
         * correspondientes con statusBarsPadding, safeDrawingPadding y el padding del Scaffold.
         */
        enableEdgeToEdge()

        /*
         * Desde aquí se construye la interfaz declarativa: el tema envuelve la navegación.
         */
        setContent {
            RedPatrimonioReligiosoTheme {
                AppNavigation()
            }
        }
    }
}
