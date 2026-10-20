package cl.duoc.redpatrimonio.navigation

object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val CATALOGO = "catalogo"
    const val MAPA = "mapa"
    const val FICHA = "ficha/{id}"
    const val EVENTOS = "eventos"
    const val FAVORITOS = "favoritos"
    const val RUTAS = "rutas"
    const val RUTA = "ruta/{id}"
    const val EDITAR_RUTA = "editar_ruta/{id}/{semilla}"
    const val VISITAS = "visitas"
    const val NUEVA_VISITA = "nueva_visita/{id}"
    const val GUIA = "guia"
    const val GESTION = "gestion"
    const val EDITAR_RECINTO = "editar_recinto/{id}"
    const val EDITAR_EVENTO = "editar_evento/{recintoId}/{eventoId}"
    const val PERFIL = "perfil"

    fun ficha(id: Int) = "ficha/$id"
    fun ruta(id: Int) = "ruta/$id"
    fun editarRuta(id: Int = -1, semilla: Int = -1) = "editar_ruta/$id/$semilla"
    fun nuevaVisita(id: Int) = "nueva_visita/$id"
    fun editarRecinto(id: Int) = "editar_recinto/$id"
    fun editarEvento(recintoId: Int, eventoId: Int = -1) = "editar_evento/$recintoId/$eventoId"
}
