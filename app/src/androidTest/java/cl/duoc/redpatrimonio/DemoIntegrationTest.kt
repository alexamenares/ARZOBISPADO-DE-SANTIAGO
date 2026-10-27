/*
 * DemoIntegrationTest.kt prueba varios flujos del prototipo usando un entorno Android.
 * Crea ViewModel, cambia roles y comprueba reglas y conservación del JSON general.
 * preparar y limpiar vacían un SharedPreferences de prueba identificado por nombre.
 * La base Room no recibe ese nombre: obtenerInstancia usa red_patrimonio.db también aquí.
 * El caso de favoritos es anterior al flujo asíncrono actual y lee resultados inmediatamente;
 * necesita revisión de esperas y aislamiento de Room antes de considerarlo una prueba fiable.
 * Esta copia documenta ese comportamiento sin modificar pruebas ni lógica de la aplicación.
 */
package cl.duoc.redpatrimonio

import android.app.Application
import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import cl.duoc.redpatrimonio.model.*
import cl.duoc.redpatrimonio.viewmodel.PatrimonioViewModel
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class DemoIntegrationTest {
    private val app get() = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
    private val nombre = "patrimonio_pruebas_aisladas"
    private val manana get() = LocalDate.now().plusDays(1).toString()
    private fun nuevaInstancia() = PatrimonioViewModel(app, nombre)

    /*
     * Esta limpieza afecta al JSON de prueba, no a la tabla favorito de Room.
     */
    @Before fun preparar() { app.getSharedPreferences(nombre, Context.MODE_PRIVATE).edit().clear().commit() }
    @After fun limpiar() { app.getSharedPreferences(nombre, Context.MODE_PRIVATE).edit().clear().commit() }

    /*
     * Intención del caso: conservar favoritos y separarlos por cuenta. Con Room las escrituras
     * y la observación son asíncronas; estas aserciones inmediatas no esperan a que terminen.
     */
    @Test fun favoritosPersistenYSeSeparanPorCuenta() {
        val primera = nuevaInstancia()
        primera.iniciarSesion(Rol.VISITANTE); primera.alternarFavorito(1)
        val segunda = nuevaInstancia()
        segunda.iniciarSesion(Rol.VISITANTE); assertTrue(1 in segunda.favoritos)
        segunda.iniciarSesion(Rol.DOCENTE); assertFalse(1 in segunda.favoritos)
    }

    /*
     * Comprueba que la restricción local del encargado impida editar el recinto ID 2.
     */
    @Test fun encargadoNoPuedeEditarOtroRecinto() {
        val vm = nuevaInstancia(); vm.iniciarSesion(Rol.ENCARGADO)
        val antes = vm.recinto(2)!!
        assertNotNull(vm.guardarRecinto(2, antes.nombre, antes.historia, antes.horario, EstadoRecinto.CERRADO, ""))
        assertEquals(antes, vm.recinto(2))
    }

    /*
     * Recorre la solicitud del docente, publicación de disponibilidad, aceptación del guía
     * y confirmación del administrador; comprueba persistencia del estado general.
     */
    @Test fun solicitudConGuiaRequiereDisponibilidadAceptacionYConfirmacion() {
        val vm = nuevaInstancia(); vm.iniciarSesion(Rol.DOCENTE)
        assertNull(vm.crearVisita(1, manana, "10:00", "20", "Entrada principal", true))
        val id = vm.estado.visitas.single().id
        vm.iniciarSesion(Rol.ADMINISTRADOR)
        assertNotNull(vm.confirmarVisita(id))
        vm.iniciarSesion(Rol.GUIA)
        assertNotNull(vm.responderGuia(id, true))
        assertNull(vm.alternarDisponibilidad(manana))
        assertNull(vm.responderGuia(id, true))
        vm.iniciarSesion(Rol.ADMINISTRADOR)
        assertNull(vm.confirmarVisita(id))
        assertEquals(EstadoVisita.CONFIRMADA, vm.estado.visitas.single().estado)
        assertEquals(vm.estado, nuevaInstancia().estado)
    }

    /*
     * Comprueba la restricción de un único recorrido asignado al guía en la misma jornada.
     */
    @Test fun guiaNoPuedeAceptarDosVisitasElMismoDia() {
        val vm = nuevaInstancia(); vm.iniciarSesion(Rol.DOCENTE)
        assertNull(vm.crearVisita(1, manana, "10:00", "10", "Entrada principal", true))
        assertNull(vm.crearVisita(1, manana, "16:00", "10", "Entrada principal", true))
        vm.iniciarSesion(Rol.GUIA); vm.alternarDisponibilidad(manana)
        assertNull(vm.responderGuia(vm.estado.visitas[0].id, true))
        assertNotNull(vm.responderGuia(vm.estado.visitas[1].id, true))
    }

    /*
     * Comprueba que una ruta usada por una visita ya no pueda editarse ni eliminarse.
     */
    @Test fun rutasConVisitasConservanHistorial() {
        val vm = nuevaInstancia(); vm.iniciarSesion(Rol.VISITANTE)
        assertNull(vm.guardarRuta(null, "Mi recorrido", listOf(1, 3)))
        val id = vm.rutasVisibles.last().id
        assertNull(vm.crearVisita(id, manana, "10:00", "2", "Entrada principal", false))
        assertNotNull(vm.guardarRuta(id, "Otro recorrido", listOf(1, 5)))
        assertNotNull(vm.eliminarRuta(id))
    }

    /*
     * Verifica que una lectura JSON inválida recupere el catálogo inicial sin cerrar la app.
     */
    @Test fun jsonCorruptoRecuperaLaDemo() {
        app.getSharedPreferences(nombre, Context.MODE_PRIVATE).edit().putString("estado", "{invalido").commit()
        assertEquals(DatosDemo.recintos, AlmacenDemo(app, nombre).cargar().recintos)
    }
}
