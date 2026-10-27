/*
 * VisitasScreens.kt contiene solicitud de visita, seguimiento y agenda del guía.
 * NuevaVisitaScreen reúne fecha, hora, asistentes y guía; muestra una revisión antes de enviar.
 * VisitasScreen filtra solicitudes y ofrece acciones según el rol y el estado de la visita.
 * GuiaScreen publica disponibilidad por día completo para el guía de demostración.
 * Las validaciones y los cambios definitivos se delegan a PatrimonioViewModel.
 */
package cl.duoc.redpatrimonio.view

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cl.duoc.redpatrimonio.model.*
import cl.duoc.redpatrimonio.navigation.Routes
import cl.duoc.redpatrimonio.viewmodel.PatrimonioViewModel
import java.time.LocalDate

/*
 * Formulario en tres pasos: datos de visita, elección de guía y revisión.
 * El envío llama a crearVisita; si resulta válido muestra la confirmación local de solicitud.
 */
@Composable
fun NuevaVisitaScreen(rutaId: Int, vm: PatrimonioViewModel, alGuardar: () -> Unit) {
    var fecha by rememberSaveable { mutableStateOf(LocalDate.now().plusDays(1).toString()) }
    var hora by rememberSaveable { mutableStateOf("10:00") }
    var personas by rememberSaveable { mutableStateOf("1") }
    var encuentro by rememberSaveable { mutableStateOf("") }
    var guia by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val ruta = vm.rutasVisibles.find { it.id == rutaId }
    var paso by rememberSaveable(rutaId) { mutableStateOf(0) }
    var enviada by rememberSaveable(rutaId) { mutableStateOf(false) }
    BackHandler(enabled = paso > 0 && !enviada) { paso--; error = null }
    Pagina {
        if (ruta == null) { Aviso("Selecciona una ruta desde Mis rutas para planificar una visita."); return@Pagina }
        if (enviada) {
            Seccion("Solicitud enviada") {
                Icono(Simbolo.CHECK, modifier = Modifier.size(56.dp))
                Text("Tu visita está pendiente de coordinación.")
                DatoFila(Simbolo.RUTA, "Ruta", ruta.nombre)
                DatoFila(Simbolo.CALENDARIO, "Fecha y hora", "$fecha · $hora")
            }
            Accion("Ver mis visitas", onClick = alGuardar)
            return@Pagina
        }
        Pasos(paso, listOf("Visita", "Guía", "Revisión"))
        Encabezado(ruta.nombre, "${ruta.paradas.size} paradas")
        when (paso) {
            0 -> {
                Campo(fecha, "Fecha (AAAA-MM-DD)", { fecha = it })
                Campo(hora, "Hora (HH:MM)", { hora = it })
                Campo(personas, "Cantidad de personas (1–50)", { personas = it }, numerico = true)
                Campo(encuentro, "Punto de encuentro", { encuentro = it })
            }
            1 -> Seccion("Acompañamiento") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = guia, onCheckedChange = { guia = it })
                    Text("Solicitar guía voluntario", Modifier.weight(1f))
                }
                if (guia) {
                    Etiqueta(if (fecha in vm.estado.disponibilidadGuia) "Disponibilidad publicada" else "Sin disponibilidad publicada")
                    Aviso("El guía debe aceptar tu solicitud. La confirmación final corresponde al administrador.")
                } else Text("Puedes solicitar la visita sin acompañamiento de guía.")
            }
            2 -> Seccion("Revisa tu solicitud") {
                DatoFila(Simbolo.CALENDARIO, "Fecha y hora", "$fecha · $hora")
                DatoFila(Simbolo.PERFIL, "Asistentes", "$personas personas")
                DatoFila(Simbolo.MAPA, "Punto de encuentro", encuentro)
                DatoFila(Simbolo.PERFIL, "Guía voluntario", if (guia) "Solicitado" else "No solicitado")
                Aviso("Al enviar, la visita quedará pendiente de coordinación.")
            }
        }
        error?.let { Aviso(it, error = true) }
        /*
         * Se valida el formulario antes de avanzar. El último paso llama al ViewModel;
         * enviada solo indica que la solicitud se guardó localmente, no una reserva en un servidor.
         */
        Accion(if (paso < 2) "Continuar" else "Enviar solicitud", dorado = true) {
            error = ReglasPatrimonio.validarVisita(fecha, hora, personas, encuentro)
            if (error == null) {
                if (paso < 2) paso++ else {
                    error = vm.crearVisita(rutaId, fecha, hora, personas, encuentro, guia)
                    if (error == null) enviada = true
                }
            }
        }
        if (paso > 0) TextButton(onClick = { paso--; error = null }) { Text("Paso anterior") }
    }
}

/*
 * Filtra las visitas visibles por estado y muestra acciones de propietario, guía o administrador.
 * Las funciones del ViewModel vuelven a validar las condiciones de cada acción.
 */
@Composable
fun VisitasScreen(vm: PatrimonioViewModel, navegar: (String) -> Unit) {
    var seleccion by rememberSaveable { mutableStateOf(0) }
    val estados = listOf<EstadoVisita?>(null, EstadoVisita.PENDIENTE, EstadoVisita.CONFIRMADA, EstadoVisita.REALIZADA, EstadoVisita.CANCELADA)
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var detalleId by rememberSaveable { mutableStateOf<Int?>(null) }
    var cancelarId by rememberSaveable { mutableStateOf<Int?>(null) }
    cancelarId?.let { id ->
        Confirmar("Cancelar visita", "La solicitud quedará cancelada en el historial.", { cancelarId = null }) {
            error = vm.cancelarVisita(id); cancelarId = null
        }
    }
    /*
     * Primero se respeta el conjunto visible según rol; después se filtra por la pestaña elegida.
     */
    val visitas = vm.visitasVisibles.filter { estados[seleccion] == null || it.estado == estados[seleccion] }
        .sortedWith(compareBy({ it.fecha }, { it.hora }))
    Pagina {
        Encabezado(if (vm.rol == Rol.GUIA) "Mis recorridos" else "Mis visitas", "Consulta el estado de cada solicitud")
        if (vm.rol != Rol.GUIA) FilaAcceso("Planificar una visita", "Elige una ruta y prepara tu solicitud", Simbolo.MAS) { navegar(Routes.RUTAS) }
        Pestanas(listOf("Todas", "Pendientes", "Confirmadas", "Realizadas", "Canceladas"), seleccion) { seleccion = it }
        error?.let { Aviso(it, error = true) }
        if (visitas.isEmpty()) Aviso(if (vm.rol == Rol.GUIA) "No hay solicitudes en esta categoría." else "No hay visitas en esta categoría. Puedes planificar una desde una ruta.")
        visitas.forEach { visita ->
            Seccion(vm.ruta(visita.rutaId)?.nombre ?: "Ruta no disponible") {
                Etiqueta(visita.estado.etiqueta)
                Text("Solicitud #${visita.id}", style = MaterialTheme.typography.labelMedium)
                DatoFila(Simbolo.CALENDARIO, "Visita", "${visita.fecha} · ${visita.hora} · ${visita.personas} personas")
                TextButton(onClick = { detalleId = if (detalleId == visita.id) null else visita.id }) {
                    Text(if (detalleId == visita.id) "Ocultar detalle" else "Ver detalle")
                }
                if (detalleId == visita.id) {
                Text("Encuentro: ${visita.encuentro}")
                Text("Solicitante: ${visita.propietario}")
                Text(when {
                    !visita.requiereGuia -> "Sin guía solicitado"
                    visita.guiaAsignado -> "Guía de demostración: aceptado"
                    visita.guiaRechazado -> "El guía rechazó la solicitud; pendiente de nueva coordinación"
                    else -> "Guía pendiente de aceptación"
                })
                /*
                 * La aceptación del guía y la confirmación administrativa son acciones diferentes.
                 * La vista ofrece el botón; el ViewModel comprueba los requisitos antes de cambiar el estado.
                 */
                if (vm.rol == Rol.ADMINISTRADOR && visita.estado == EstadoVisita.PENDIENTE)
                    Accion("Confirmar visita") { error = vm.confirmarVisita(visita.id) }
                if (vm.rol == Rol.ADMINISTRADOR && visita.estado == EstadoVisita.CONFIRMADA)
                    Accion("Marcar como realizada") { error = vm.finalizarVisita(visita.id) }
                if (visita.estado in listOf(EstadoVisita.PENDIENTE, EstadoVisita.CONFIRMADA) &&
                    (visita.propietario == vm.cuenta || vm.rol == Rol.ADMINISTRADOR))
                    TextButton(onClick = { cancelarId = visita.id }) { Text("Cancelar visita") }
                if (vm.rol == Rol.GUIA && visita.requiereGuia && visita.estado == EstadoVisita.PENDIENTE) {
                    Accion(if (visita.guiaAsignado) "Retirar aceptación" else "Aceptar solicitud") {
                        error = vm.responderGuia(visita.id, !visita.guiaAsignado)
                    }
                    if (!visita.guiaAsignado && !visita.guiaRechazado)
                        TextButton(onClick = { error = vm.responderGuia(visita.id, false) }) { Text("Rechazar solicitud") }
                }
                }
            }
        }
    }
}

/*
 * Permite agregar o retirar días de disponibilidad. Los interruptores son accesos rápidos
 * a la misma acción del ViewModel; la demo admite un recorrido por jornada.
 */
@Composable
fun GuiaScreen(vm: PatrimonioViewModel, navegar: (String) -> Unit) {
    var fecha by rememberSaveable { mutableStateOf(LocalDate.now().plusDays(1).toString()) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    Pagina {
        if (vm.rol != Rol.GUIA) { Aviso("Esta pantalla corresponde al perfil de guía."); return@Pagina }
        Encabezado("Mi disponibilidad", "Selecciona los días en que puedes guiar")
        Seccion("Próximos 7 días") {
            val formato = DateTimeFormatter.ofPattern("EEEE d MMM", Locale.forLanguageTag("es-CL"))
            (1L..7L).forEach { desplazamiento ->
                val dia = LocalDate.now().plusDays(desplazamiento)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(dia.format(formato), Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = dia.toString() in vm.estado.disponibilidadGuia,
                        onCheckedChange = { error = vm.alternarDisponibilidad(dia.toString()) })
                }
            }
        }
        Seccion("Mi disponibilidad") {
            Text("Disponibilidad por jornada completa. La demo permite un recorrido por día.")
            Campo(fecha, "Fecha (AAAA-MM-DD)", { fecha = it })
            Accion(if (fecha in vm.estado.disponibilidadGuia) "Retirar esta fecha" else "Agregar fecha disponible") {
                error = vm.alternarDisponibilidad(fecha)
            }
            error?.let { Aviso(it, error = true) }
        }
        val fechas = vm.estado.disponibilidadGuia.filter { it >= LocalDate.now().toString() }.sorted()
        if (fechas.isEmpty()) Aviso("Todavía no has indicado fechas disponibles.")
        fechas.forEach { dia ->
            Seccion(dia) { Text("Disponible para un recorrido")
                TextButton(onClick = { error = vm.alternarDisponibilidad(dia) }) { Text("Retirar disponibilidad") }
            }
        }
        Accion("Ver solicitudes y recorridos") { navegar(Routes.VISITAS) }
    }
}
