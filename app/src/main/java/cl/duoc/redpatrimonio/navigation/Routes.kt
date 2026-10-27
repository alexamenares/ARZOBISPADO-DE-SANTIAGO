/*
 * Routes.kt es el catálogo de direcciones internas de la app.
 * Un object mantiene constantes y funciones accesibles como Routes.FICHA o Routes.ficha(1).
 * Las constantes con llaves son plantillas; las funciones reemplazan los parámetros por IDs.
 * AppNavigation registra las pantallas y las vistas solicitan navegar usando estas rutas.
 * Para explicarlo: define los nombres de los destinos, no la pantalla ni su contenido.
 */
package cl.duoc.redpatrimonio.navigation

/**
 * Centraliza los nombres de las rutas usadas por Navigation Compose.
 *
 * Mantenerlas aquí evita escribir textos de rutas repetidos o con errores
 * dentro de las pantallas de la aplicación.
 */
object Routes {

    // Pantallas principales de acceso y exploración.
    /*
     * LOGIN está declarado como nombre disponible; en esta implementación el login
     * se muestra fuera del NavHost cuando vm.rol es null, mediante AppNavigation.
     */
    const val LOGIN = "login"
    const val HOME = "home"
    const val CATALOGO = "catalogo"
    const val MAPA = "mapa"
    const val EVENTOS = "eventos"
    const val FAVORITOS = "favoritos"
    const val RUTAS = "rutas"
    const val VISITAS = "visitas"
    const val GUIA = "guia"
    const val GESTION = "gestion"
    const val PERFIL = "perfil"

    // Pantalla que utilizará la cámara para leer códigos QR.
    const val QR_SCANNER = "qr_scanner"

    // Rutas que reciben datos variables, como el id de un recinto o una ruta.
    const val FICHA = "ficha/{id}"
    const val RUTA = "ruta/{id}"
    const val EDITAR_RUTA = "editar_ruta/{id}/{semilla}"
    const val NUEVA_VISITA = "nueva_visita/{id}"
    const val EDITAR_RECINTO = "editar_recinto/{id}"
    const val EDITAR_EVENTO = "editar_evento/{recintoId}/{eventoId}"

    /**
     * Construye la ruta para ver la ficha de un recinto específico.
     */
    fun ficha(id: Int) = "ficha/$id"

    /**
     * Construye la ruta para ver el detalle de una ruta patrimonial.
     */
    fun ruta(id: Int) = "ruta/$id"

    /**
     * Construye la ruta para crear o editar una ruta patrimonial.
     */
    /*
     * id=-1 indica creación. semilla es el primer recinto opcional, por ejemplo
     * cuando se elige Crear ruta desde aquí dentro de una ficha.
     */
    fun editarRuta(
        id: Int = -1,
        semilla: Int = -1
    ) = "editar_ruta/$id/$semilla"

    /**
     * Construye la ruta para crear una visita asociada a una ruta.
     */
    fun nuevaVisita(id: Int) = "nueva_visita/$id"

    /**
     * Construye la ruta para editar un recinto.
     */
    fun editarRecinto(id: Int) = "editar_recinto/$id"

    /**
     * Construye la ruta para crear o editar un evento de un recinto.
     */
    fun editarEvento(
        recintoId: Int,
        eventoId: Int = -1
    ) = "editar_evento/$recintoId/$eventoId"
}
