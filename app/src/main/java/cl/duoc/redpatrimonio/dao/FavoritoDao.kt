/*
 * FavoritoDao.kt declara las operaciones SQL de favoritos.
 * DAO significa objeto de acceso a datos: conecta la tabla con el código Kotlin.
 * @Insert guarda y @Query permite leer o eliminar mediante SQL con parámetros.
 * Room y KSP generan la implementación; no se escriben esos archivos generados a mano.
 * suspend permite suspender escrituras en una corrutina. Flow emite listas actualizadas.
 * Para explicarlo: aquí se define cómo consultar la tabla; el repositorio usa estas operaciones.
 */
package cl.duoc.redpatrimonio.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import cl.duoc.redpatrimonio.model.FavoritoEntity
import kotlinx.coroutines.flow.Flow

/**
 * Define las operaciones permitidas sobre la tabla "favorito".
 *
 * Room genera automáticamente la implementación de esta interfaz.
 */
@Dao
interface FavoritoDao {

    /**
     * Guarda un favorito.
     * Si ya existe el mismo recinto para el mismo usuario, lo ignora
     * para evitar duplicados.
     */
    /*
     * El índice único de FavoritoEntity hace que IGNORE pueda evitar favoritos repetidos.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertarFavorito(favorito: FavoritoEntity)

    /**
     * Elimina un favorito usando el usuario y el id del recinto.
     */
    @Query(
        """
        DELETE FROM favorito
        WHERE correoUsuario = :correoUsuario
        AND recintoId = :recintoId
        """
    )
    /*
     * Los nombres con dos puntos dentro del SQL son parámetros que Room recibe de esta función.
     */
    suspend fun eliminarFavorito(
        correoUsuario: String,
        recintoId: Int
    )

    /**
     * Entrega los ids de los recintos favoritos del usuario.
     *
     * Flow permite que la interfaz se actualice automáticamente
     * cuando cambien los datos almacenados.
     */
    @Query(
        """
        SELECT recintoId FROM favorito
        WHERE correoUsuario = :correoUsuario
        ORDER BY id DESC
        """
    )
    fun obtenerIdsFavoritos(correoUsuario: String): Flow<List<Int>>
}
