/*
 * AppDatabase.kt configura y abre la base local Room.
 * Incluye FavoritoEntity, ofrece FavoritoDao y usa el archivo red_patrimonio.db.
 * Room trabaja sobre SQLite; BundledSQLiteDriver aporta el controlador de SQLite.
 * La instancia se conserva en memoria para reutilizarla dentro del proceso de la app.
 * Para explicarlo: este archivo abre la base; el DAO contiene las consultas.
 * No contiene las tablas Oracle ni conecta con el backend.
 */
package cl.duoc.redpatrimonio.database

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import cl.duoc.redpatrimonio.dao.FavoritoDao
import cl.duoc.redpatrimonio.model.FavoritoEntity

/**
 * Punto central de acceso a la base de datos local de la aplicación.
 *
 * Actualmente contiene solo la tabla de favoritos. En el futuro se pueden
 * agregar nuevas entidades, por ejemplo para rutas creadas por el usuario.
 */
@Database(
    entities = [FavoritoEntity::class],
    /*
     * Versión del esquema local. Una modificación de tablas requiere planificar su migración.
     * exportSchema=false evita exportar el esquema a archivos JSON durante la compilación.
     */
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Entrega las operaciones disponibles sobre la tabla favorito.
     */
    abstract fun favoritoDao(): FavoritoDao

    companion object {

        // Evita crear más de una base de datos mientras la app está abierta.
        /*
         * Hace visibles los cambios de esta referencia entre hilos. synchronized protege
         * el bloque de creación; estos mecanismos se refieren a la instancia, no a cada fila.
         */
        @Volatile
        private var instancia: AppDatabase? = null

        /**
         * Obtiene la única instancia de la base de datos de la aplicación.
         */
        fun obtenerInstancia(context: Context): AppDatabase {
            return instancia ?: synchronized(this) {
                /*
                 * Usa el contexto de toda la aplicación y no conserva una referencia a una pantalla.
                 */
                val appContext = context.applicationContext
                val archivoBaseDatos =
                    appContext.getDatabasePath("red_patrimonio.db")

                val nuevaInstancia = Room.databaseBuilder<AppDatabase>(
                    context = appContext,
                    name = archivoBaseDatos.absolutePath
                )
                    // Define el controlador de SQLite configurado anteriormente.
                    .setDriver(BundledSQLiteDriver())
                    .build()

                instancia = nuevaInstancia
                nuevaInstancia
            }
        }
    }
}
