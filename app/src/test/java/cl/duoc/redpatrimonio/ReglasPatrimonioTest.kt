package cl.duoc.redpatrimonio

import cl.duoc.redpatrimonio.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDateTime

class ReglasPatrimonioTest {
    private val ahora = LocalDateTime.of(2030, 1, 1, 10, 0)

    @Test fun busquedaIgnoraTildesYMayusculas() {
        assertEquals(listOf(2), ReglasPatrimonio.buscar(DatosDemo.recintos, "  NUNOA ", false, "Todos").map { it.id })
    }

    @Test fun filtroAbiertosExcluyeCelebracionesYCierres() {
        val encontrados = ReglasPatrimonio.buscar(DatosDemo.recintos, "", true, "Todos")
        assertEquals(setOf(1, 3, 5, 6), encontrados.map { it.id }.toSet())
    }

    @Test fun combinaTextoTipoYEstado() {
        assertEquals(listOf(1), ReglasPatrimonio.buscar(DatosDemo.recintos, "santiago", true, "Iglesia").map { it.id })
        assertTrue(ReglasPatrimonio.buscar(DatosDemo.recintos, "santiago", true, "Museo").isEmpty())
    }

    @Test fun rutaRechazaDuplicadosYRecintosDesconocidos() {
        assertNotNull(ReglasPatrimonio.validarRuta("Ruta de prueba", listOf(1, 1), setOf(1, 2)))
        assertNotNull(ReglasPatrimonio.validarRuta("Ruta de prueba", listOf(1, 9), setOf(1, 2)))
        assertNull(ReglasPatrimonio.validarRuta("Ruta de prueba", listOf(1, 2), setOf(1, 2)))
    }

    @Test fun rutaExigeNombreYDosParadas() {
        assertNotNull(ReglasPatrimonio.validarRuta("  ", listOf(1, 2), setOf(1, 2)))
        assertNotNull(ReglasPatrimonio.validarRuta("Ruta", listOf(1), setOf(1, 2)))
    }

    @Test fun moverConservaLasParadasSinMutarElOriginal() {
        val original = listOf(1, 3, 5)
        assertEquals(listOf(3, 1, 5), ReglasPatrimonio.moverParada(original, 1, -1))
        assertEquals(listOf(1, 5, 3), ReglasPatrimonio.moverParada(original, 1, 1))
        assertEquals(listOf(1, 3, 5), original)
    }

    @Test fun moverFueraDeLimitesNoCambiaLaRuta() {
        assertEquals(listOf(1, 2), ReglasPatrimonio.moverParada(listOf(1, 2), 0, -1))
        assertEquals(listOf(1, 2), ReglasPatrimonio.moverParada(listOf(1, 2), 1, 1))
    }

    @Test fun fechaHoraEsEstricta() {
        assertNull(ReglasPatrimonio.fechaHora("2030-02-30", "10:00"))
        assertNull(ReglasPatrimonio.fechaHora("2030-01-02", "24:00"))
        assertNull(ReglasPatrimonio.fechaHora("2030-01-02", "9:00"))
        assertNotNull(ReglasPatrimonio.fechaHora("2032-02-29", "09:00"))
    }

    @Test fun visitaExigeFechaFutura() {
        assertNotNull(ReglasPatrimonio.validarVisita("2030-01-01", "09:59", "2", "Entrada principal", ahora))
        assertNotNull(ReglasPatrimonio.validarVisita("2030-01-01", "10:00", "2", "Entrada principal", ahora))
        assertNull(ReglasPatrimonio.validarVisita("2030-01-01", "10:01", "2", "Entrada principal", ahora))
    }

    @Test fun visitaValidaCantidadYEncuentro() {
        listOf("", "abc", "0", "51", "999999999999999999").forEach {
            assertNotNull(ReglasPatrimonio.validarVisita("2030-01-02", "10:00", it, "Entrada", ahora))
        }
        assertNotNull(ReglasPatrimonio.validarVisita("2030-01-02", "10:00", "3", " ", ahora))
        assertNull(ReglasPatrimonio.validarVisita("2030-01-02", "10:00", "50", "Entrada", ahora))
    }

    @Test fun estimacionIncluyeEstanciasYTrayectos() {
        assertEquals(0, ReglasPatrimonio.minutosEstimados(0))
        assertEquals(25, ReglasPatrimonio.minutosEstimados(1))
        assertEquals(105, ReglasPatrimonio.minutosEstimados(3))
    }
}
