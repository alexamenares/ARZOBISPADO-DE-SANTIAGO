/*
 * FavoritoEntity.kt define la única tabla Room de esta versión: favorito.
 * Cada fila relaciona una cuenta de demostración con el identificador de un recinto.
 * @Entity indica que es una tabla; @PrimaryKey identifica cada fila.
 * El índice único impide repetir el mismo recinto para una misma cuenta.
 * FavoritoDao usa esta clase para guardar datos en red_patrimonio.db.
 */
package cl.duoc.redpatrimonio.model

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

/**
 * Representa un recinto que un usuario marcó como favorito.
 *
 * Esta clase se almacenará como la tabla "favorito" dentro de Room.
 * No guarda el recinto completo: solo su identificador, porque los datos
 * del recinto ya existen en los datos de demostración de la aplicación.
 */
@Entity(
    tableName = "favorito",
    /*
     * El índice es único para la pareja cuenta y recinto: usuarios diferentes sí pueden
     * guardar el mismo recinto. El DAO ignora la inserción si esa pareja ya existe.
     */
    indices = [
        Index(
            value = ["correoUsuario", "recintoId"],
            unique = true
        )
    ]
)
data class FavoritoEntity(

    // Identificador interno autogenerado para cada registro de la tabla.
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Identificador de la cuenta demo, por ejemplo visitante; el nombre del campo es heredado.
    val correoUsuario: String,

    // Corresponde al id del objeto Recinto que el usuario marcó como favorito.
    val recintoId: Int
)
