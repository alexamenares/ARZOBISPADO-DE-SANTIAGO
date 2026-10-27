/*
 * ReglasPatrimonioTest.kt verifica reglas de negocio con JUnit en la máquina de desarrollo.
 * No abre pantallas ni requiere emulador: prueba funciones Kotlin independientes de Android.
 * @Test marca cada caso; assertEquals compara valores, assertNull espera éxito de validación
 * y assertNotNull espera un mensaje de error. El reloj fijo vuelve repetibles los casos de fecha.
 * Estos tests son diferentes de los instrumentados de la carpeta androidTest.
 */
package cl.duoc.redpatrimonio

import cl.duoc.redpatrimonio.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime

class ReglasPatrimonioTest {
    private val ahora = LocalDateTime.of(2030, 1, 1, 10, 0)

    /*
     * Comprueba que la normalización permita encontrar Ñuñoa escribiendo NUNOA.
     */
    @Test fun busquedaIgnoraTildesYMayusculas() {
        assertEquals(listOf(2), ReglasPatrimonio.buscar(DatosDemo.recintos, "  NUNOA ", false, "Todos").map { it.id })
    }

    /*
     * Verifica que Solo abiertos excluya recintos cerrados o en celebración.
     */
    @Test fun filtroAbiertosExcluyeCelebracionesYCierres() {
        val encontrados = ReglasPatrimonio.buscar(DatosDemo.recintos, "", true, "Todos")
        assertEquals(setOf(1, 3, 5, 6), encontrados.map { it.id }.toSet())
    }

    /*
     * Comprueba que los filtros de búsqueda se apliquen al mismo tiempo.
     */
    @Test fun combinaTextoTipoYEstado() {
        assertEquals(listOf(1), ReglasPatrimonio.buscar(DatosDemo.recintos, "santiago", true, "Iglesia").map { it.id })
        assertTrue(ReglasPatrimonio.buscar(DatosDemo.recintos, "santiago", true, "Museo").isEmpty())
    }

    /*
     * Valida IDs de las paradas y ausencia de duplicados en una ruta.
     */
    @Test fun rutaRechazaDuplicadosYRecintosDesconocidos() {
        assertNotNull(ReglasPatrimonio.validarRuta("Ruta de prueba", listOf(1, 1), setOf(1, 2)))
        assertNotNull(ReglasPatrimonio.validarRuta("Ruta de prueba", listOf(1, 9), setOf(1, 2)))
        assertNull(ReglasPatrimonio.validarRuta("Ruta de prueba", listOf(1, 2), setOf(1, 2)))
    }

    /*
     * Rechaza rutas sin nombre adecuado o con menos de dos paradas.
     */
    @Test fun rutaExigeNombreYDosParadas() {
        assertNotNull(ReglasPatrimonio.validarRuta("  ", listOf(1, 2), setOf(1, 2)))
        assertNotNull(ReglasPatrimonio.validarRuta("Ruta", listOf(1), setOf(1, 2)))
    }

    /*
     * Confirma que el cambio de orden produzca otra lista sin alterar la original.
     */
    @Test fun moverConservaLasParadasSinMutarElOriginal() {
        val original = listOf(1, 3, 5)
        assertEquals(listOf(3, 1, 5), ReglasPatrimonio.moverParada(original, 1, -1))
        assertEquals(listOf(1, 5, 3), ReglasPatrimonio.moverParada(original, 1, 1))
        assertEquals(listOf(1, 3, 5), original)
    }

    /*
     * Evita subir la primera parada o bajar la última más allá del recorrido.
     */
    @Test fun moverFueraDeLimitesNoCambiaLaRuta() {
        assertEquals(listOf(1, 2), ReglasPatrimonio.moverParada(listOf(1, 2), 0, -1))
        assertEquals(listOf(1, 2), ReglasPatrimonio.moverParada(listOf(1, 2), 1, 1))
    }

    /*
     * Rechaza días inexistentes y horas inválidas, y admite una fecha bisiesta válida.
     */
    @Test fun fechaHoraEsEstricta() {
        assertNull(ReglasPatrimonio.fechaHora("2030-02-30", "10:00"))
        assertNull(ReglasPatrimonio.fechaHora("2030-01-02", "24:00"))
        assertNull(ReglasPatrimonio.fechaHora("2030-01-02", "9:00"))
        assertNotNull(ReglasPatrimonio.fechaHora("2032-02-29", "09:00"))
    }

    /*
     * Compara inicio de visita con un momento fijo anterior, igual o posterior.
     */
    @Test fun visitaExigeFechaFutura() {
        assertNotNull(ReglasPatrimonio.validarVisita("2030-01-01", "09:59", "2", "Entrada principal", ahora))
        assertNotNull(ReglasPatrimonio.validarVisita("2030-01-01", "10:00", "2", "Entrada principal", ahora))
        assertNull(ReglasPatrimonio.validarVisita("2030-01-01", "10:01", "2", "Entrada principal", ahora))
    }

    /*
     * Comprueba cantidades inválidas, desbordamiento y punto de encuentro obligatorio.
     */
    @Test fun visitaValidaCantidadYEncuentro() {
        listOf("", "abc", "0", "51", "999999999999999999").forEach {
            assertNotNull(ReglasPatrimonio.validarVisita("2030-01-02", "10:00", it, "Entrada", ahora))
        }
        assertNotNull(ReglasPatrimonio.validarVisita("2030-01-02", "10:00", "3", " ", ahora))
        assertNull(ReglasPatrimonio.validarVisita("2030-01-02", "10:00", "50", "Entrada", ahora))
    }

    /*
     * Verifica la fórmula académica de duración, no una medición geográfica real.
     */
    @Test fun estimacionIncluyeEstanciasYTrayectos() {
        assertEquals(0, ReglasPatrimonio.minutosEstimados(0))
        assertEquals(25, ReglasPatrimonio.minutosEstimados(1))
        assertEquals(105, ReglasPatrimonio.minutosEstimados(3))
    }
}
