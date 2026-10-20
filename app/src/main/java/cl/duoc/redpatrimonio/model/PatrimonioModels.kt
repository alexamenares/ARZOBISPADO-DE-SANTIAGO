package cl.duoc.redpatrimonio.model

enum class Rol(val etiqueta: String, val cuenta: String) {
    VISITANTE("Visitante", "visitante"), DOCENTE("Docente", "docente"),
    GUIA("Guía", "guia"), ENCARGADO("Encargado", "encargado"),
    ADMINISTRADOR("Administrador", "admin")
}

enum class EstadoRecinto(val etiqueta: String) {
    ABIERTO("Abierto"), CERRADO("Cerrado"), EN_CELEBRACION("En celebración")
}

data class Recinto(
    val id: Int, val nombre: String, val tipo: String, val comuna: String,
    val direccion: String, val historia: String, val horario: String,
    val estado: EstadoRecinto, val aviso: String = ""
)

data class Evento(
    val id: Int, val recintoId: Int, val titulo: String,
    val fecha: String, val hora: String, val descripcion: String, val cancelado: Boolean = false
)

data class RutaPatrimonial(
    val id: Int, val nombre: String, val paradas: List<Int>,
    val sugerida: Boolean = false, val propietario: String = "admin"
)

enum class EstadoVisita(val etiqueta: String) {
    PENDIENTE("Pendiente de coordinación"), CONFIRMADA("Confirmada"),
    CANCELADA("Cancelada"), REALIZADA("Realizada")
}

data class Visita(
    val id: Int, val rutaId: Int, val fecha: String, val hora: String,
    val personas: Int, val encuentro: String, val requiereGuia: Boolean,
    val estado: EstadoVisita = EstadoVisita.PENDIENTE,
    val guiaAsignado: Boolean = false, val propietario: String = "admin",
    val guiaRechazado: Boolean = false
)

data class EstadoDemo(
    val recintos: List<Recinto> = DatosDemo.recintos,
    val eventos: List<Evento> = DatosDemo.eventos(),
    val rutas: List<RutaPatrimonial> = DatosDemo.rutas,
    val visitas: List<Visita> = emptyList(),
    val favoritos: Map<String, Set<Int>> = emptyMap(),
    val disponibilidadGuia: Set<String> = emptySet()
)
