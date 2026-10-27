/*
 * PatrimonioModels.kt define los datos y estados del prototipo.
 * Las data class agrupan información; los enum class limitan las opciones válidas.
 * Las pantallas leen estos objetos y PatrimonioViewModel crea copias al modificarlos.
 * Solo FavoritoEntity es una tabla Room. Las clases de este archivo no llevan @Entity.
 * Para explicarlo: este archivo define qué información maneja la app, no cómo se dibuja.
 */
package cl.duoc.redpatrimonio.model

/*
 * Perfiles de la demo. etiqueta es el texto visible y cuenta es el identificador
 * que se escribe en el login y se usa para separar favoritos y rutas.
 */
enum class Rol(val etiqueta: String, val cuenta: String) {
    VISITANTE("Visitante", "visitante"), DOCENTE("Docente", "docente"),
    GUIA("Guía", "guia"), ENCARGADO("Encargado", "encargado"),
    ADMINISTRADOR("Administrador", "admin")
}

/*
 * Opciones de apertura usadas por filtros y validaciones de visitas.
 */
enum class EstadoRecinto(val etiqueta: String) {
    ABIERTO("Abierto"), CERRADO("Cerrado"), EN_CELEBRACION("En celebración")
}

/*
 * Ficha patrimonial. id relaciona el recinto con eventos, rutas, favoritos y QR.
 */
data class Recinto(
    val id: Int, val nombre: String, val tipo: String, val comuna: String,
    val direccion: String, val historia: String, val horario: String,
    val estado: EstadoRecinto, val aviso: String = ""
)

/*
 * Actividad asociada a un recinto por recintoId. cancelado conserva el historial.
 */
data class Evento(
    val id: Int, val recintoId: Int, val titulo: String,
    val fecha: String, val hora: String, val descripcion: String, val cancelado: Boolean = false
)

/*
 * Recorrido: paradas contiene IDs de recintos en orden. sugerida distingue las rutas
 * iniciales; propietario identifica la cuenta de demo que puede editar la ruta.
 */
data class RutaPatrimonial(
    val id: Int, val nombre: String, val paradas: List<Int>,
    val sugerida: Boolean = false, val propietario: String = "admin"
)

/*
 * Estados de la solicitud: pendiente, confirmada, cancelada o realizada.
 */
enum class EstadoVisita(val etiqueta: String) {
    PENDIENTE("Pendiente de coordinación"), CONFIRMADA("Confirmada"),
    CANCELADA("Cancelada"), REALIZADA("Realizada")
}

/*
 * Solicitud vinculada a una ruta. Solicitar guía no significa que esté asignado:
 * el guía debe aceptar y el administrador confirma después.
 */
data class Visita(
    val id: Int, val rutaId: Int, val fecha: String, val hora: String,
    val personas: Int, val encuentro: String, val requiereGuia: Boolean,
    val estado: EstadoVisita = EstadoVisita.PENDIENTE,
    val guiaAsignado: Boolean = false, val propietario: String = "admin",
    val guiaRechazado: Boolean = false
)

/*
 * Agrupa el estado general que AlmacenDemo guarda como JSON. El campo favoritos
 * es heredado: la interfaz actual usa PatrimonioViewModel.favoritos, obtenido de Room.
 */
data class EstadoDemo(
    val recintos: List<Recinto> = DatosDemo.recintos,
    val eventos: List<Evento> = DatosDemo.eventos(),
    val rutas: List<RutaPatrimonial> = DatosDemo.rutas,
    val visitas: List<Visita> = emptyList(),
    val favoritos: Map<String, Set<Int>> = emptyMap(),
    val disponibilidadGuia: Set<String> = emptySet()
)
