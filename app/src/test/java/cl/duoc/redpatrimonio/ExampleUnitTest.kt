/*
 * ExampleUnitTest.kt es la prueba local básica de la plantilla Android Studio.
 * Comprueba una suma con JUnit y se ejecuta en la máquina de desarrollo.
 * No demuestra por sí sola el funcionamiento de Room, navegación o QR.
 * Las reglas propias del proyecto se prueban en ReglasPatrimonioTest.kt.
 */
package cl.duoc.redpatrimonio

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    /*
     * Ejemplo mínimo de aserción: el resultado esperado es 4.
     */
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}
