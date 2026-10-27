/*
 * ExampleInstrumentedTest.kt es una prueba de plantilla que necesita Android.
 * Se ejecuta en emulador o dispositivo con AndroidJUnit4.
 * Obtiene el contexto instalado y comprueba el nombre del paquete de esta aplicación.
 * No escanea un QR ni navega automáticamente por las pantallas.
 */
package cl.duoc.redpatrimonio

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    /*
     * Verifica que el contexto de la aplicación tenga el applicationId esperado.
     */
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("cl.duoc.redpatrimonio", appContext.packageName)
    }
}
