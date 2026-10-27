/*
 * PatrimonioViewModel.kt coordina el estado y las acciones principales de la app.
 * Las pantallas llaman a sus funciones, en vez de escribir directamente en la persistencia.
 * Usa dos mecanismos: AlmacenDemo para el estado general y FavoritoRepository con Room
 * para los favoritos de cada cuenta. viewModelScope.launch inicia el trabajo con Room.
 * mutableStateOf permite que Compose reaccione al nuevo estado; copy crea datos actualizados.
 * Rol identifica una cuenta de demostración por perfil, no usuarios reales del backend.
 * Las operaciones que devuelven String? usan null como éxito y un texto como error.
 */
package cl.duoc.redpatrimonio.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.redpatrimonio.database.AppDatabase
import cl.duoc.redpatrimonio.model.*
import cl.duoc.redpatrimonio.repository.FavoritoRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * ViewModel principal de la aplicación.
 *
 * Administra el estado de los recintos, rutas, visitas y eventos del demo.
 * Los favoritos se almacenan de forma independiente usando Room.
 */
class PatrimonioViewModel private constructor(
    application: Application,
    private val almacen: AlmacenDemo
) : AndroidViewModel(application) {

    /*
     * AndroidViewModel recibe Application para acceder al almacenamiento sin depender
     * de una pantalla concreta. El constructor público prepara el almacén habitual.
     */
    constructor(application: Application) : this(
        application,
        AlmacenDemo(application)
    )

    // Almacén separado para pruebas instrumentadas, sin tocar los datos de la app.
    internal constructor(application: Application, nombreAlmacen: String) : this(
        application,
        AlmacenDemo(application, nombreAlmacen)
    )

    /**
     * Repository que conecta este ViewModel con Room para gestionar favoritos.
     */
    private val favoritoRepository = FavoritoRepository(
        AppDatabase.obtenerInstancia(application).favoritoDao()
    )

    /**
     * Permite detener la observación de favoritos al cambiar de cuenta
     * o al cerrar sesión.
     */
    private var trabajoFavoritos: Job? = null

    /**
     * Estado general del demo: rutas, visitas, eventos y recintos.
     * Sigue utilizando AlmacenDemo por ahora.
     */
    /*
     * Al asignar un nuevo EstadoDemo, Compose vuelve a evaluar las vistas que lo leen.
     * private set evita reemplazar este estado directamente desde las pantallas.
     */
    var estado by mutableStateOf(almacen.cargar())
        private set

    /**
     * Rol de la cuenta que inició sesión actualmente.
     */
    var rol by mutableStateOf<Rol?>(null)
        private set

    /**
     * Correo o identificador de la cuenta activa.
     */
    val cuenta: String
        get() = rol?.cuenta.orEmpty()

    /*
     * Propiedad calculada: se obtiene del rol actual cada vez que se consulta.
     */
    val puedeGestionar: Boolean
        get() = rol == Rol.ADMINISTRADOR || rol == Rol.ENCARGADO

    /**
     * Ids de los recintos favoritos de la cuenta activa.
     *
     * Esta información se obtiene desde Room y no desde AlmacenDemo.
     */
    var favoritos by mutableStateOf<Set<Int>>(emptySet())
        private set

    /*
     * Cada cuenta ve las rutas sugeridas y las personales que le pertenecen.
     */
    val rutasVisibles: List<RutaPatrimonial>
        get() = estado.rutas.filter {
            it.sugerida || it.propietario == cuenta
        }

    /*
     * El administrador ve todo; cada solicitante ve lo suyo; el guía ve solicitudes con guía.
     * Son reglas del prototipo local y no sustituyen la autorización de un futuro backend.
     */
    val visitasVisibles: List<Visita>
        get() = estado.visitas.filter {
            rol == Rol.ADMINISTRADOR ||
                    it.propietario == cuenta ||
                    (rol == Rol.GUIA && it.requiereGuia)
        }

    /**
     * Inicia sesión y comienza a observar los favoritos de esa cuenta.
     */
    fun iniciarSesion(nuevoRol: Rol) {
        rol = nuevoRol
        observarFavoritos(nuevoRol.cuenta)
    }

    /**
     * Cierra sesión, detiene la observación de Room y limpia los favoritos
     * visibles para que no se muestren los de la cuenta anterior.
     */
    fun cerrarSesion() {
        trabajoFavoritos?.cancel()
        favoritos = emptySet()
        rol = null
    }

    /**
     * Guarda los cambios del estado general en AlmacenDemo.
     */
    private fun actualizar(nuevo: EstadoDemo) {
        almacen.guardar(nuevo)
        estado = nuevo
    }

    /**
     * Escucha los favoritos almacenados en Room.
     *
     * Flow emite una nueva lista cada vez que Room detecta un cambio
     * en la tabla favorito, actualizando la interfaz automáticamente.
     */
    private fun observarFavoritos(correoUsuario: String) {
        trabajoFavoritos?.cancel()

        /*
         * launch inicia una corrutina ligada a la vida del ViewModel. collect escucha Room
         * y transforma los IDs en un conjunto para comprobar favoritos sin duplicados.
         */
        trabajoFavoritos = viewModelScope.launch {
            favoritoRepository.obtenerIdsFavoritos(correoUsuario).collect { ids ->
                favoritos = ids.toSet()
            }
        }
    }

    /*
     * Busca un recinto por ID; devuelve null si no existe. Se usa también al recibir el QR.
     */
    fun recinto(id: Int) = estado.recintos.find { it.id == id }

    /*
     * Busca una ruta patrimonial por ID y permite detectar rutas que ya no existen.
     */
    fun ruta(id: Int) = estado.rutas.find { it.id == id }

    /*
     * Regla de la demo: el administrador edita todos los recintos; el encargado solo el ID 1.
     */
    fun puedeEditar(recintoId: Int) =
        rol == Rol.ADMINISTRADOR || (rol == Rol.ENCARGADO && recintoId == 1)

    /*
     * Una ruta solo se edita si es personal y pertenece a la cuenta activa.
     */
    fun puedeEditarRuta(ruta: RutaPatrimonial) =
        !ruta.sugerida && ruta.propietario == cuenta

    /**
     * Agrega o elimina un recinto de los favoritos de la cuenta activa.
     *
     * La información se guarda en Room para que permanezca disponible
     * aunque la aplicación se cierre y se abra nuevamente.
     */
    fun alternarFavorito(id: Int) {
        if (rol == null || recinto(id) == null) return

        /*
         * Se captura la cuenta antes de lanzar la corrutina, para guardar el favorito
         * con el identificador que estaba activo al pulsar el botón.
         */
        val correoUsuario = cuenta

        viewModelScope.launch {
            if (id in favoritos) {
                favoritoRepository.quitarFavorito(
                    correoUsuario = correoUsuario,
                    recintoId = id
                )
            } else {
                favoritoRepository.guardarFavorito(
                    correoUsuario = correoUsuario,
                    recintoId = id
                )
            }
        }
    }

    /*
     * Crea o reemplaza una ruta después de validar nombre, paradas y propietario.
     * Si ya tiene visitas, evita editarla para conservar su historial. null significa éxito.
     */
    fun guardarRuta(id: Int?, nombre: String, paradas: List<Int>): String? {
        if (rol == null) return "Inicia sesión para guardar una ruta."

        ReglasPatrimonio.validarRuta(
            nombre,
            paradas,
            estado.recintos.map { it.id }.toSet()
        )?.let {
            return it
        }

        val anterior = id?.let { ruta(it) }

        if (id != null && (anterior == null || !puedeEditarRuta(anterior))) {
            return "Esta ruta no se puede editar."
        }

        if (id != null && estado.visitas.any { it.rutaId == id }) {
            return "Esta ruta tiene visitas asociadas. Crea una ruta nueva para conservar su historial."
        }

        val nueva = RutaPatrimonial(
            id = id ?: ((estado.rutas.maxOfOrNull { it.id } ?: 0) + 1),
            nombre = nombre.trim(),
            paradas = paradas.toList(),
            propietario = cuenta
        )

        actualizar(
            estado.copy(
                rutas = if (id == null) {
                    estado.rutas + nueva
                } else {
                    estado.rutas.map { if (it.id == id) nueva else it }
                }
            )
        )

        return null
    }

    /*
     * Elimina una ruta propia únicamente si no tiene visitas asociadas.
     * filterNot crea la lista resultante y actualizar la conserva en el almacén de la demo.
     */
    fun eliminarRuta(id: Int): String? {
        val ruta = ruta(id) ?: return "La ruta ya no existe."

        if (!puedeEditarRuta(ruta)) {
            return "Solo puedes eliminar tus propias rutas."
        }

        if (estado.visitas.any { it.rutaId == id }) {
            return "La ruta tiene visitas asociadas y se conserva para mantener su historial."
        }

        actualizar(
            estado.copy(
                rutas = estado.rutas.filterNot { it.id == id }
            )
        )

        return null
    }

    /*
     * Valida la solicitud y comprueba que la ruta sea visible, sus recintos estén abiertos
     * y la cuenta no tenga otra visita activa a la misma fecha y hora. Se crea PENDIENTE.
     */
    fun crearVisita(
        rutaId: Int,
        fecha: String,
        hora: String,
        personas: String,
        encuentro: String,
        requiereGuia: Boolean
    ): String? {
        if (rol == null || rutasVisibles.none { it.id == rutaId }) {
            return "Selecciona una ruta disponible."
        }

        ReglasPatrimonio.validarVisita(
            fecha,
            hora,
            personas,
            encuentro
        )?.let {
            return it
        }

        val ruta = ruta(rutaId) ?: return "La ruta ya no existe."

        if (ruta.paradas.any { recinto(it)?.estado != EstadoRecinto.ABIERTO }) {
            return "La ruta incluye recintos cerrados o en celebración. Elige otra ruta o espera su apertura."
        }

        if (
            estado.visitas.any {
                it.propietario == cuenta &&
                        it.fecha == fecha &&
                        it.hora == hora &&
                        it.estado != EstadoVisita.CANCELADA
            }
        ) {
            return "Ya tienes una visita para esa fecha y hora."
        }

        val visita = Visita(
            id = (estado.visitas.maxOfOrNull { it.id } ?: 0) + 1,
            rutaId = rutaId,
            fecha = fecha,
            hora = hora,
            personas = personas.toInt(),
            encuentro = encuentro.trim(),
            requiereGuia = requiereGuia,
            propietario = cuenta
        )

        actualizar(
            estado.copy(
                visitas = estado.visitas + visita
            )
        )

        return null
    }

    /*
     * Permite al propietario o administrador cancelar una visita pendiente o confirmada.
     * Conserva el registro en el historial y retira su asignación de guía.
     */
    fun cancelarVisita(id: Int): String? {
        val visita = estado.visitas.find { it.id == id }
            ?: return "La visita ya no existe."

        if (visita.propietario != cuenta && rol != Rol.ADMINISTRADOR) {
            return "No puedes cancelar esta visita."
        }

        if (visita.estado !in listOf(EstadoVisita.PENDIENTE, EstadoVisita.CONFIRMADA)) {
            return "La visita ya finalizó o fue cancelada."
        }

        reemplazarVisita(
            visita.copy(
                estado = EstadoVisita.CANCELADA,
                guiaAsignado = false
            )
        )

        return null
    }

    /*
     * Solo el administrador confirma. Exige solicitud pendiente, fecha futura, recintos
     * abiertos y, si se solicitó guía, que el guía ya haya aceptado.
     */
    fun confirmarVisita(id: Int): String? {
        if (rol != Rol.ADMINISTRADOR) {
            return "Esta acción corresponde al administrador."
        }

        val visita = estado.visitas.find { it.id == id }
            ?: return "La visita ya no existe."

        if (visita.estado != EstadoVisita.PENDIENTE) {
            return "Solo se confirman solicitudes pendientes."
        }

        if (visita.requiereGuia && !visita.guiaAsignado) {
            return "El guía debe aceptar la solicitud antes de confirmarla."
        }

        if (
            ReglasPatrimonio.fechaHora(visita.fecha, visita.hora)
                ?.isAfter(LocalDateTime.now()) != true
        ) {
            return "La fecha de la visita ya pasó."
        }

        if (
            ruta(visita.rutaId)
                ?.paradas
                ?.all { recinto(it)?.estado == EstadoRecinto.ABIERTO } != true
        ) {
            return "Revisa el estado de apertura de los recintos."
        }

        reemplazarVisita(
            visita.copy(
                estado = EstadoVisita.CONFIRMADA
            )
        )

        return null
    }

    /*
     * El guía acepta o rechaza una solicitud pendiente. Para aceptar necesita publicar
     * ese día como disponible y no tener otro recorrido asignado ese día en la demo.
     */
    fun responderGuia(id: Int, aceptar: Boolean): String? {
        if (rol != Rol.GUIA) {
            return "Esta acción corresponde al guía."
        }

        val visita = estado.visitas.find { it.id == id }
            ?: return "La visita ya no existe."

        if (!visita.requiereGuia || visita.estado != EstadoVisita.PENDIENTE) {
            return "La solicitud ya no está pendiente."
        }

        if (aceptar) {
            if (visita.fecha !in estado.disponibilidadGuia) {
                return "Agrega primero esta fecha a tu disponibilidad."
            }

            if (
                ReglasPatrimonio.fechaHora(visita.fecha, visita.hora)
                    ?.isAfter(LocalDateTime.now()) != true
            ) {
                return "La fecha de la visita ya pasó."
            }

            // Un único guía de demostración, con disponibilidad por jornada completa.
            if (
                estado.visitas.any {
                    it.id != id &&
                            it.fecha == visita.fecha &&
                            it.guiaAsignado &&
                            it.estado != EstadoVisita.CANCELADA
                }
            ) {
                return "Ya tienes otro recorrido asignado ese día."
            }
        }

        reemplazarVisita(
            visita.copy(
                guiaAsignado = aceptar,
                guiaRechazado = !aceptar
            )
        )

        return null
    }

    /*
     * El administrador marca como realizada una visita confirmada cuando ya comenzó.
     * La comprobación usa la fecha y hora del dispositivo.
     */
    fun finalizarVisita(id: Int): String? {
        if (rol != Rol.ADMINISTRADOR) {
            return "Esta acción corresponde al administrador."
        }

        val visita = estado.visitas.find { it.id == id }
            ?: return "La visita ya no existe."

        if (visita.estado != EstadoVisita.CONFIRMADA) {
            return "Solo se finalizan visitas confirmadas."
        }

        if (
            ReglasPatrimonio.fechaHora(visita.fecha, visita.hora)
                ?.isAfter(LocalDateTime.now()) != false
        ) {
            return "La visita todavía no comienza."
        }

        reemplazarVisita(
            visita.copy(
                estado = EstadoVisita.REALIZADA
            )
        )

        return null
    }

    /*
     * Actualiza una visita por ID usando copy y map, conservando las demás solicitudes.
     */
    private fun reemplazarVisita(visita: Visita) {
        actualizar(
            estado.copy(
                visitas = estado.visitas.map {
                    if (it.id == visita.id) visita else it
                }
            )
        )
    }

    /*
     * Agrega o retira un día completo de disponibilidad del guía. Valida el formato
     * y la fecha, e impide retirar un día con un recorrido asignado que no esté cancelado.
     */
    fun alternarDisponibilidad(fecha: String): String? {
        if (rol != Rol.GUIA) {
            return "Esta acción corresponde al guía."
        }

        val dia = try {
            LocalDate.parse(fecha)
        } catch (_: java.time.DateTimeException) {
            return "Usa una fecha válida AAAA-MM-DD."
        }

        if (dia.isBefore(LocalDate.now())) {
            return "La fecha no puede ser anterior a hoy."
        }

        if (
            fecha in estado.disponibilidadGuia &&
            estado.visitas.any {
                it.fecha == fecha &&
                        it.guiaAsignado &&
                        it.estado != EstadoVisita.CANCELADA
            }
        ) {
            return "Tienes un recorrido asignado ese día. Cancela la asignación pendiente o contacta al administrador."
        }

        val dias = if (fecha in estado.disponibilidadGuia) {
            estado.disponibilidadGuia - fecha
        } else {
            estado.disponibilidadGuia + fecha
        }

        actualizar(
            estado.copy(
                disponibilidadGuia = dias
            )
        )

        return null
    }

    /*
     * Valida permiso y longitudes antes de actualizar nombre, historia, horario y aviso.
     * Los demás datos del recinto se conservan con copy.
     */
    fun guardarRecinto(
        id: Int,
        nombre: String,
        historia: String,
        horario: String,
        apertura: EstadoRecinto,
        aviso: String
    ): String? {
        if (!puedeEditar(id)) {
            return "No tienes permiso para modificar este recinto."
        }

        if (
            nombre.trim().length !in 3..100 ||
            historia.trim().length !in 20..2000 ||
            horario.trim().length !in 5..300 ||
            aviso.length > 300
        ) {
            return "Revisa nombre (3–100), historia (20–2000), horario (5–300) y aviso (hasta 300 caracteres)."
        }

        val recinto = recinto(id) ?: return "El recinto ya no existe."

        actualizar(
            estado.copy(
                recintos = estado.recintos.map {
                    if (it.id == id) {
                        recinto.copy(
                            nombre = nombre.trim(),
                            historia = historia.trim(),
                            horario = horario.trim(),
                            estado = apertura,
                            aviso = aviso.trim()
                        )
                    } else {
                        it
                    }
                }
            )
        )

        return null
    }

    /*
     * Crea o modifica un evento del recinto autorizado. Exige título, descripción
     * y fecha futura; un ID nulo representa un evento nuevo.
     */
    fun guardarEvento(
        id: Int?,
        recintoId: Int,
        titulo: String,
        fecha: String,
        hora: String,
        descripcion: String
    ): String? {
        if (!puedeEditar(recintoId) || recinto(recintoId) == null) {
            return "No puedes publicar en ese recinto."
        }

        if (id != null && estado.eventos.none {
                it.id == id && it.recintoId == recintoId
            }
        ) {
            return "El evento ya no existe."
        }

        if (
            titulo.trim().length !in 3..100 ||
            descripcion.trim().length !in 10..1000
        ) {
            return "Escribe un título (3–100) y una descripción (10–1000 caracteres)."
        }

        val inicio = ReglasPatrimonio.fechaHora(fecha, hora)
            ?: return "Usa una fecha válida AAAA-MM-DD y una hora HH:MM."

        if (!inicio.isAfter(LocalDateTime.now())) {
            return "El evento debe comenzar en el futuro."
        }

        val evento = Evento(
            id = id ?: ((estado.eventos.maxOfOrNull { it.id } ?: 0) + 1),
            recintoId = recintoId,
            titulo = titulo.trim(),
            fecha = fecha,
            hora = hora,
            descripcion = descripcion.trim()
        )

        actualizar(
            estado.copy(
                eventos = if (id == null) {
                    estado.eventos + evento
                } else {
                    estado.eventos.map {
                        if (it.id == id) evento else it
                    }
                }
            )
        )

        return null
    }

    /*
     * Marca el evento como cancelado, conservando la publicación para el historial.
     */
    fun cancelarEvento(id: Int): String? {
        val evento = estado.eventos.find { it.id == id }
            ?: return "El evento ya no existe."

        if (!puedeEditar(evento.recintoId)) {
            return "No puedes modificar ese evento."
        }

        actualizar(
            estado.copy(
                eventos = estado.eventos.map {
                    if (it.id == id) it.copy(cancelado = true) else it
                }
            )
        )
        return null
    }

    /**
     * Restablece los datos generales del demo.
     *
     * Los favoritos de Room se conservan, pues pertenecen a la base de datos
     * local y no al JSON general de la demostración.
     */
    fun reiniciarDemo() {
        /*
         * Esta restauración vuelve a EstadoDemo inicial. No elimina las filas de favoritos
         * de Room: el repositorio de favoritos no participa en esta operación.
         */
        if (rol == Rol.ADMINISTRADOR) {
            actualizar(EstadoDemo())
        }
    }
}
