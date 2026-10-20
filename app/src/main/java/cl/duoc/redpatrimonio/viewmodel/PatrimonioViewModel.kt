package cl.duoc.redpatrimonio.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import cl.duoc.redpatrimonio.model.*
import java.time.LocalDate
import java.time.LocalDateTime

class PatrimonioViewModel private constructor(application: Application, private val almacen: AlmacenDemo) : AndroidViewModel(application) {
    constructor(application: Application) : this(application, AlmacenDemo(application))
    // Almacén separado para pruebas instrumentadas, sin tocar los datos de la app.
    internal constructor(application: Application, nombreAlmacen: String) : this(application, AlmacenDemo(application, nombreAlmacen))
    var estado by mutableStateOf(almacen.cargar())
        private set
    var rol by mutableStateOf<Rol?>(null)
        private set
    val cuenta: String get() = rol?.cuenta.orEmpty()
    val puedeGestionar: Boolean get() = rol == Rol.ADMINISTRADOR || rol == Rol.ENCARGADO
    val favoritos: Set<Int> get() = estado.favoritos[cuenta].orEmpty()
    val rutasVisibles: List<RutaPatrimonial> get() = estado.rutas.filter { it.sugerida || it.propietario == cuenta }
    val visitasVisibles: List<Visita> get() = estado.visitas.filter {
        rol == Rol.ADMINISTRADOR || it.propietario == cuenta || (rol == Rol.GUIA && it.requiereGuia)
    }

    fun iniciarSesion(nuevoRol: Rol) { rol = nuevoRol }
    fun cerrarSesion() { rol = null }
    private fun actualizar(nuevo: EstadoDemo) { almacen.guardar(nuevo); estado = nuevo }
    fun recinto(id: Int) = estado.recintos.find { it.id == id }
    fun ruta(id: Int) = estado.rutas.find { it.id == id }
    fun puedeEditar(recintoId: Int) = rol == Rol.ADMINISTRADOR || (rol == Rol.ENCARGADO && recintoId == 1)
    fun puedeEditarRuta(ruta: RutaPatrimonial) = !ruta.sugerida && ruta.propietario == cuenta

    fun alternarFavorito(id: Int) {
        if (rol == null || recinto(id) == null) return
        val nuevos = if (id in favoritos) favoritos - id else favoritos + id
        actualizar(estado.copy(favoritos = estado.favoritos + (cuenta to nuevos)))
    }

    fun guardarRuta(id: Int?, nombre: String, paradas: List<Int>): String? {
        if (rol == null) return "Inicia sesión para guardar una ruta."
        ReglasPatrimonio.validarRuta(nombre, paradas, estado.recintos.map { it.id }.toSet())?.let { return it }
        val anterior = id?.let { ruta(it) }
        if (id != null && (anterior == null || !puedeEditarRuta(anterior))) return "Esta ruta no se puede editar."
        if (id != null && estado.visitas.any { it.rutaId == id }) return "Esta ruta tiene visitas asociadas. Crea una ruta nueva para conservar su historial."
        val nueva = RutaPatrimonial(id ?: ((estado.rutas.maxOfOrNull { it.id } ?: 0) + 1), nombre.trim(), paradas.toList(), propietario = cuenta)
        actualizar(estado.copy(rutas = if (id == null) estado.rutas + nueva else estado.rutas.map { if (it.id == id) nueva else it }))
        return null
    }

    fun eliminarRuta(id: Int): String? {
        val ruta = ruta(id) ?: return "La ruta ya no existe."
        if (!puedeEditarRuta(ruta)) return "Solo puedes eliminar tus propias rutas."
        if (estado.visitas.any { it.rutaId == id }) return "La ruta tiene visitas asociadas y se conserva para mantener su historial."
        actualizar(estado.copy(rutas = estado.rutas.filterNot { it.id == id }))
        return null
    }

    fun crearVisita(rutaId: Int, fecha: String, hora: String, personas: String, encuentro: String, requiereGuia: Boolean): String? {
        if (rol == null || rutasVisibles.none { it.id == rutaId }) return "Selecciona una ruta disponible."
        ReglasPatrimonio.validarVisita(fecha, hora, personas, encuentro)?.let { return it }
        val ruta = ruta(rutaId) ?: return "La ruta ya no existe."
        if (ruta.paradas.any { recinto(it)?.estado != EstadoRecinto.ABIERTO }) return "La ruta incluye recintos cerrados o en celebración. Elige otra ruta o espera su apertura."
        if (estado.visitas.any { it.propietario == cuenta && it.fecha == fecha && it.hora == hora && it.estado != EstadoVisita.CANCELADA })
            return "Ya tienes una visita para esa fecha y hora."
        val visita = Visita((estado.visitas.maxOfOrNull { it.id } ?: 0) + 1, rutaId, fecha, hora,
            personas.toInt(), encuentro.trim(), requiereGuia, propietario = cuenta)
        actualizar(estado.copy(visitas = estado.visitas + visita))
        return null
    }

    fun cancelarVisita(id: Int): String? {
        val visita = estado.visitas.find { it.id == id } ?: return "La visita ya no existe."
        if (visita.propietario != cuenta && rol != Rol.ADMINISTRADOR) return "No puedes cancelar esta visita."
        if (visita.estado !in listOf(EstadoVisita.PENDIENTE, EstadoVisita.CONFIRMADA)) return "La visita ya finalizó o fue cancelada."
        reemplazarVisita(visita.copy(estado = EstadoVisita.CANCELADA, guiaAsignado = false))
        return null
    }

    fun confirmarVisita(id: Int): String? {
        if (rol != Rol.ADMINISTRADOR) return "Esta acción corresponde al administrador."
        val visita = estado.visitas.find { it.id == id } ?: return "La visita ya no existe."
        if (visita.estado != EstadoVisita.PENDIENTE) return "Solo se confirman solicitudes pendientes."
        if (visita.requiereGuia && !visita.guiaAsignado) return "El guía debe aceptar la solicitud antes de confirmarla."
        if (ReglasPatrimonio.fechaHora(visita.fecha, visita.hora)?.isAfter(LocalDateTime.now()) != true) return "La fecha de la visita ya pasó."
        if (ruta(visita.rutaId)?.paradas?.all { recinto(it)?.estado == EstadoRecinto.ABIERTO } != true) return "Revisa el estado de apertura de los recintos."
        reemplazarVisita(visita.copy(estado = EstadoVisita.CONFIRMADA))
        return null
    }

    fun responderGuia(id: Int, aceptar: Boolean): String? {
        if (rol != Rol.GUIA) return "Esta acción corresponde al guía."
        val visita = estado.visitas.find { it.id == id } ?: return "La visita ya no existe."
        if (!visita.requiereGuia || visita.estado != EstadoVisita.PENDIENTE) return "La solicitud ya no está pendiente."
        if (aceptar) {
            if (visita.fecha !in estado.disponibilidadGuia) return "Agrega primero esta fecha a tu disponibilidad."
            if (ReglasPatrimonio.fechaHora(visita.fecha, visita.hora)?.isAfter(LocalDateTime.now()) != true) return "La fecha de la visita ya pasó."
            // Un único guía de demostración, con disponibilidad por jornada completa.
            if (estado.visitas.any { it.id != id && it.fecha == visita.fecha && it.guiaAsignado && it.estado != EstadoVisita.CANCELADA })
                return "Ya tienes otro recorrido asignado ese día."
        }
        reemplazarVisita(visita.copy(guiaAsignado = aceptar, guiaRechazado = !aceptar))
        return null
    }

    fun finalizarVisita(id: Int): String? {
        if (rol != Rol.ADMINISTRADOR) return "Esta acción corresponde al administrador."
        val visita = estado.visitas.find { it.id == id } ?: return "La visita ya no existe."
        if (visita.estado != EstadoVisita.CONFIRMADA) return "Solo se finalizan visitas confirmadas."
        if (ReglasPatrimonio.fechaHora(visita.fecha, visita.hora)?.isAfter(LocalDateTime.now()) != false) return "La visita todavía no comienza."
        reemplazarVisita(visita.copy(estado = EstadoVisita.REALIZADA))
        return null
    }

    private fun reemplazarVisita(visita: Visita) = actualizar(estado.copy(visitas = estado.visitas.map { if (it.id == visita.id) visita else it }))

    fun alternarDisponibilidad(fecha: String): String? {
        if (rol != Rol.GUIA) return "Esta acción corresponde al guía."
        val dia = try { LocalDate.parse(fecha) } catch (_: java.time.DateTimeException) { return "Usa una fecha válida AAAA-MM-DD." }
        if (dia.isBefore(LocalDate.now())) return "La fecha no puede ser anterior a hoy."
        if (fecha in estado.disponibilidadGuia && estado.visitas.any { it.fecha == fecha && it.guiaAsignado && it.estado != EstadoVisita.CANCELADA })
            return "Tienes un recorrido asignado ese día. Cancela la asignación pendiente o contacta al administrador."
        val dias = if (fecha in estado.disponibilidadGuia) estado.disponibilidadGuia - fecha else estado.disponibilidadGuia + fecha
        actualizar(estado.copy(disponibilidadGuia = dias))
        return null
    }

    fun guardarRecinto(id: Int, nombre: String, historia: String, horario: String, apertura: EstadoRecinto, aviso: String): String? {
        if (!puedeEditar(id)) return "No tienes permiso para modificar este recinto."
        if (nombre.trim().length !in 3..100 || historia.trim().length !in 20..2000 || horario.trim().length !in 5..300 || aviso.length > 300)
            return "Revisa nombre (3–100), historia (20–2000), horario (5–300) y aviso (hasta 300 caracteres)."
        val recinto = recinto(id) ?: return "El recinto ya no existe."
        actualizar(estado.copy(recintos = estado.recintos.map {
            if (it.id == id) recinto.copy(nombre = nombre.trim(), historia = historia.trim(), horario = horario.trim(), estado = apertura, aviso = aviso.trim()) else it
        }))
        return null
    }

    fun guardarEvento(id: Int?, recintoId: Int, titulo: String, fecha: String, hora: String, descripcion: String): String? {
        if (!puedeEditar(recintoId) || recinto(recintoId) == null) return "No puedes publicar en ese recinto."
        if (id != null && estado.eventos.none { it.id == id && it.recintoId == recintoId }) return "El evento ya no existe."
        if (titulo.trim().length !in 3..100 || descripcion.trim().length !in 10..1000) return "Escribe un título (3–100) y una descripción (10–1000 caracteres)."
        val inicio = ReglasPatrimonio.fechaHora(fecha, hora) ?: return "Usa una fecha válida AAAA-MM-DD y una hora HH:MM."
        if (!inicio.isAfter(LocalDateTime.now())) return "El evento debe comenzar en el futuro."
        val evento = Evento(id ?: ((estado.eventos.maxOfOrNull { it.id } ?: 0) + 1), recintoId, titulo.trim(), fecha, hora, descripcion.trim())
        actualizar(estado.copy(eventos = if (id == null) estado.eventos + evento else estado.eventos.map { if (it.id == id) evento else it }))
        return null
    }

    fun cancelarEvento(id: Int): String? {
        val evento = estado.eventos.find { it.id == id } ?: return "El evento ya no existe."
        if (!puedeEditar(evento.recintoId)) return "No puedes modificar ese evento."
        actualizar(estado.copy(eventos = estado.eventos.map { if (it.id == id) it.copy(cancelado = true) else it }))
        return null
    }

    fun reiniciarDemo() { if (rol == Rol.ADMINISTRADOR) actualizar(EstadoDemo()) }
}
