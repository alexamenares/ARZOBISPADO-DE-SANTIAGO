/*
 * GestionScreens.kt agrupa las vistas de administración, edición y perfil.
 * GestionScreen muestra los recintos permitidos y sus eventos; los editores recogen cambios.
 * PerfilScreen permite abrir favoritos, cerrar sesión y, al administrador, restablecer la demo.
 * Los campos se mantienen como estado local; guardar llama al ViewModel para validar y persistir.
 * Para explicarlo: organiza formularios y acciones de los perfiles de gestión.
 */
package cl.duoc.redpatrimonio.view

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import cl.duoc.redpatrimonio.model.*
import cl.duoc.redpatrimonio.navigation.Routes
import cl.duoc.redpatrimonio.viewmodel.PatrimonioViewModel
import java.time.LocalDate

/*
 * Lista recintos editables y sus publicaciones. Pide confirmación para cancelar un evento.
 */
@Composable
fun GestionScreen(vm: PatrimonioViewModel, navegar: (String) -> Unit) {
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var cancelarId by rememberSaveable { mutableStateOf<Int?>(null) }
    cancelarId?.let { id ->
        Confirmar("Cancelar evento", "El evento seguirá visible en el historial como cancelado.", { cancelarId = null }) {
            error = vm.cancelarEvento(id); cancelarId = null
        }
    }
    Pagina {
        if (!vm.puedeGestionar) { Aviso("Esta pantalla corresponde al encargado o administrador."); return@Pagina }
        Aviso(if (vm.rol == Rol.ENCARGADO) "Tu recinto asignado es Templo San Gabriel."
            else "Puedes gestionar todos los recintos de demostración.")
        error?.let { Aviso(it, error = true) }
        vm.estado.recintos.filter { vm.puedeEditar(it.id) }.forEach { recinto ->
            Seccion(recinto.nombre) {
                Text("Estado: ${recinto.estado.etiqueta}")
                Accion("Editar ficha, horarios y avisos") { navegar(Routes.editarRecinto(recinto.id)) }
                Accion("Publicar evento") { navegar(Routes.editarEvento(recinto.id)) }
            }
            vm.estado.eventos.filter { it.recintoId == recinto.id }.forEach { evento ->
                Seccion(evento.titulo) {
                    Text("${evento.fecha} · ${evento.hora}")
                    if (evento.cancelado) Text("Cancelado") else {
                        Accion("Editar evento") { navegar(Routes.editarEvento(recinto.id, evento.id)) }
                        TextButton(onClick = { cancelarId = evento.id }) { Text("Cancelar evento") }
                    }
                }
            }
        }
        if (vm.rol == Rol.ADMINISTRADOR) Accion("Coordinar solicitudes de visita") { navegar(Routes.VISITAS) }
    }
}

/*
 * Carga campos desde el recinto seleccionado. Los cambios quedan en el formulario
 * hasta pulsar Guardar; el ViewModel valida y conserva el resultado.
 */
@Composable
fun EditarRecintoScreen(id: Int, vm: PatrimonioViewModel, volver: () -> Unit) {
    val recinto = vm.recinto(id)
    var nombre by rememberSaveable(id) { mutableStateOf(recinto?.nombre.orEmpty()) }
    var historia by rememberSaveable(id) { mutableStateOf(recinto?.historia.orEmpty()) }
    var horario by rememberSaveable(id) { mutableStateOf(recinto?.horario.orEmpty()) }
    var aviso by rememberSaveable(id) { mutableStateOf(recinto?.aviso.orEmpty()) }
    var apertura by rememberSaveable(id) { mutableStateOf((recinto?.estado ?: EstadoRecinto.CERRADO).name) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    Pagina {
        if (recinto == null || !vm.puedeEditar(id)) { Aviso("No puedes editar este recinto."); return@Pagina }
        Campo(nombre, "Nombre", { nombre = it })
        Campo(historia, "Historia", { historia = it }, multilinea = true)
        Campo(horario, "Horarios habituales o especiales", { horario = it }, multilinea = true)
        Text("Estado del recinto", style = MaterialTheme.typography.titleMedium)
        EstadoRecinto.values().forEach { estado ->
            FilterChip(selected = apertura == estado.name, onClick = { apertura = estado.name }, label = { Text(estado.etiqueta) })
        }
        Campo(aviso, "Aviso especial (opcional)", { aviso = it }, multilinea = true)
        error?.let { Aviso(it, error = true) }
        Accion("Guardar cambios") {
            /*
             * Las cadenas se envían al ViewModel, que comprueba permisos y longitudes.
             * El campo error actualiza el aviso; un resultado null permite volver.
             */
            error = vm.guardarRecinto(id, nombre, historia, horario, EstadoRecinto.valueOf(apertura), aviso)
            if (error == null) volver()
        }
    }
}

/*
 * Reutiliza el formulario para crear o editar. eventoId=-1 indica creación.
 * El ViewModel verifica autorización y que la fecha de inicio esté en el futuro.
 */
@Composable
fun EditarEventoScreen(recintoId: Int, eventoId: Int, vm: PatrimonioViewModel, volver: () -> Unit) {
    val evento = vm.estado.eventos.find { it.id == eventoId && it.recintoId == recintoId }
    var titulo by rememberSaveable(eventoId) { mutableStateOf(evento?.titulo.orEmpty()) }
    var fecha by rememberSaveable(eventoId) { mutableStateOf(evento?.fecha ?: LocalDate.now().plusDays(7).toString()) }
    var hora by rememberSaveable(eventoId) { mutableStateOf(evento?.hora ?: "12:00") }
    var descripcion by rememberSaveable(eventoId) { mutableStateOf(evento?.descripcion.orEmpty()) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    Pagina {
        if (!vm.puedeEditar(recintoId) || vm.recinto(recintoId) == null || (eventoId != -1 && evento == null)) {
            Aviso("Este evento no está disponible para edición."); return@Pagina
        }
        Text(vm.recinto(recintoId)?.nombre.orEmpty(), style = MaterialTheme.typography.titleLarge)
        Campo(titulo, "Título del evento", { titulo = it })
        Campo(fecha, "Fecha (AAAA-MM-DD)", { fecha = it })
        Campo(hora, "Hora (HH:MM)", { hora = it })
        Campo(descripcion, "Descripción", { descripcion = it }, multilinea = true)
        error?.let { Aviso(it, error = true) }
        Accion("Guardar evento") {
            error = vm.guardarEvento(eventoId.takeIf { it != -1 }, recintoId, titulo, fecha, hora, descripcion)
            if (error == null) volver()
        }
    }
}

/*
 * Muestra la cuenta activa y ofrece accesos y diálogos de confirmación.
 * Salir llama a la función recibida desde AppNavigation; no borra los datos guardados.
 */
@Composable
fun PerfilScreen(vm: PatrimonioViewModel, navegar: (String) -> Unit, salir: () -> Unit) {
    var confirmarReset by rememberSaveable { mutableStateOf(false) }
    var confirmarSalida by rememberSaveable { mutableStateOf(false) }
    /*
     * El texto de este diálogo es heredado y menciona favoritos. La implementación actual
     * de reiniciarDemo restablece el JSON general y conserva los favoritos de Room.
     * Se documenta la diferencia sin cambiar textos ni comportamiento en esta copia comentada.
     */
    if (confirmarReset) Confirmar("Restablecer demostración",
        "Se borrarán favoritos, rutas personales, visitas y modificaciones de TODOS los perfiles en este dispositivo.",
        { confirmarReset = false }) { vm.reiniciarDemo(); confirmarReset = false }
    if (confirmarSalida) Confirmar("Cerrar sesión", "Los cambios guardados se conservarán en este dispositivo.",
        { confirmarSalida = false }) { confirmarSalida = false; salir() }
    Pagina {
        Seccion("Mi perfil") {
            Text(vm.rol?.etiqueta.orEmpty(), style = MaterialTheme.typography.headlineSmall)
            Text("Cuenta de demostración: ${vm.cuenta}")
            Text("${vm.favoritos.size} favoritos · ${vm.estado.rutas.count { !it.sugerida && it.propietario == vm.cuenta }} rutas personales")
        }
        FilaAcceso("Mis favoritos", "Tus recintos guardados", Simbolo.FAVORITO) { navegar(Routes.FAVORITOS) }
        FilaAcceso("Mis rutas", "Recorridos sugeridos y personales", Simbolo.RUTA) { navegar(Routes.RUTAS) }
        if (vm.puedeGestionar) FilaAcceso("Administrar recintos y eventos", "Fichas y publicaciones", Simbolo.EDIFICIO) { navegar(Routes.GESTION) }
        if (vm.rol == Rol.GUIA) FilaAcceso("Mi disponibilidad", "Organiza tu agenda", Simbolo.CALENDARIO) { navegar(Routes.GUIA) }
        Seccion("Acerca de la demostración") {
            Text("Proyecto académico de Aplicaciones Móviles. Kotlin, Jetpack Compose y MVVM.")
            Text("La información se guarda solo en este dispositivo. No hay autenticación real, sincronización, reservas externas ni ubicación GPS.")
        }
        Accion("Cerrar sesión") { confirmarSalida = true }
        if (vm.rol == Rol.ADMINISTRADOR)
            TextButton(onClick = { confirmarReset = true }) { Text("Restablecer todos los datos de ejemplo") }
    }
}
