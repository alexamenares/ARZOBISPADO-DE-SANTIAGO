/*
 * FavorityRepository.kt contiene la clase FavoritoRepository.
 * Se conserva el nombre original del archivo; las importaciones usan el nombre de la clase.
 * El repositorio recibe un DAO y ofrece acciones con nombres claros al ViewModel:
 * guardarFavorito, quitarFavorito y obtenerIdsFavoritos.
 * Para explicarlo: es el intermediario entre las acciones de la app y las consultas SQL.
 */
package cl.duoc.redpatrimonio.repository

import cl.duoc.redpatrimonio.dao.FavoritoDao
import cl.duoc.redpatrimonio.model.FavoritoEntity
import kotlinx.coroutines.flow.Flow

/**
 * Intermedia entre el ViewModel y el DAO de favoritos.
 *
 * Aquí se agrupan las acciones relacionadas con los favoritos,
 * evitando que las pantallas o ViewModels dependan directamente de Room.
 */
class FavoritoRepository(
    private val favoritoDao: FavoritoDao
) {

    /**
     * Guarda un recinto como favorito para el usuario indicado.
     */
    suspend fun guardarFavorito(
        correoUsuario: String,
        recintoId: Int
    ) {
        val favorito = FavoritoEntity(
            correoUsuario = correoUsuario,
            recintoId = recintoId
        )

        favoritoDao.insertarFavorito(favorito)
    }

    /**
     * Quita un recinto de los favoritos del usuario indicado.
     */
    suspend fun quitarFavorito(
        correoUsuario: String,
        recintoId: Int
    ) {
        favoritoDao.eliminarFavorito(
            correoUsuario = correoUsuario,
            recintoId = recintoId
        )
    }

    /**
     * Observa los ids de los recintos favoritos del usuario.
     */
    fun obtenerIdsFavoritos(
        correoUsuario: String
    ): Flow<List<Int>> {
        return favoritoDao.obtenerIdsFavoritos(correoUsuario)
    }
}
