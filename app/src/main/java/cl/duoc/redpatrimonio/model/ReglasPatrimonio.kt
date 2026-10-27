/*
 * ReglasPatrimonio.kt reúne validaciones y cálculos independientes de Android.
 * Normaliza búsquedas, valida rutas y visitas, interpreta fechas y reordena paradas.
 * Lo usan las pantallas y PatrimonioViewModel. Se prueba con ReglasPatrimonioTest.
 * Las validaciones devuelven null si todo está bien o un mensaje con el problema.
 * Para explicarlo: aquí están las reglas reutilizables, sin dibujar pantallas ni guardar datos.
 */
package cl.duoc.redpatrimonio.model

import java.text.Normalizer
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle
import java.util.Locale

/** Reglas independientes de Android, para poder probarlas con JUnit. */
object ReglasPatrimonio {
    /*
     * Quita espacios de los extremos, tildes y diferencias de mayúsculas.
     * Así la búsqueda NUNOA puede encontrar Ñuñoa.
     */
    fun normalizar(texto: String): String = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
        .replace("\\p{M}".toRegex(), "").lowercase(Locale.ROOT)

    /*
     * Combina búsqueda por nombre, tipo o comuna con los filtros de apertura y tipo.
     * filter entrega otra lista sin modificar el catálogo original.
     */
    fun buscar(recintos: List<Recinto>, consulta: String, soloAbiertos: Boolean, tipo: String): List<Recinto> {
        val texto = normalizar(consulta)
        return recintos.filter {
            (!soloAbiertos || it.estado == EstadoRecinto.ABIERTO) &&
                (tipo == "Todos" || it.tipo == tipo) &&
                normalizar("${it.nombre} ${it.tipo} ${it.comuna}").contains(texto)
        }
    }

    /*
     * Exige nombre de 3 a 80 caracteres y entre 2 y 6 paradas válidas, sin duplicados.
     * Devuelve el primer problema encontrado o null si la ruta es válida.
     */
    fun validarRuta(nombre: String, paradas: List<Int>, idsValidos: Set<Int>): String? = when {
        nombre.trim().length !in 3..80 -> "Escribe un nombre de entre 3 y 80 caracteres."
        paradas.size !in 2..6 -> "Selecciona entre 2 y 6 recintos."
        paradas.distinct().size != paradas.size -> "Cada recinto puede aparecer una sola vez."
        paradas.any { it !in idsValidos } -> "La ruta contiene un recinto que ya no está disponible."
        else -> null
    }

    /*
     * Acepta fecha AAAA-MM-DD y hora HH:MM. Si no se pueden interpretar, devuelve null.
     * Permite validar antes de crear una visita o publicar un evento.
     */
    fun fechaHora(fecha: String, hora: String): LocalDateTime? = try {
        if (!fecha.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) || !hora.matches(Regex("\\d{2}:\\d{2}"))) null
        else LocalDateTime.of(LocalDate.parse(fecha),
            LocalTime.parse(hora, DateTimeFormatter.ofPattern("HH:mm").withResolverStyle(ResolverStyle.STRICT)))
    } catch (_: java.time.DateTimeException) { null }

    /*
     * Valida fecha futura, entre 1 y 50 personas y punto de encuentro de 3 a 150 caracteres.
     * El parámetro ahora permite probar la función con un reloj fijo en los tests.
     */
    fun validarVisita(fecha: String, hora: String, personas: String, encuentro: String,
        ahora: LocalDateTime = LocalDateTime.now()): String? {
        val inicio = fechaHora(fecha, hora) ?: return "Usa una fecha válida AAAA-MM-DD y una hora HH:MM."
        return when {
            !inicio.isAfter(ahora) -> "La visita debe comenzar en una fecha y hora futuras."
            (personas.toIntOrNull() ?: 0) !in 1..50 -> "La cantidad de personas debe estar entre 1 y 50."
            encuentro.trim().length !in 3..150 -> "Indica un punto de encuentro de entre 3 y 150 caracteres."
            else -> null
        }
    }

    // Supuesto académico explícito: 25 minutos por recinto y 15 entre paradas.
    fun minutosEstimados(cantidadParadas: Int): Int =
        if (cantidadParadas <= 0) 0 else cantidadParadas * 25 + (cantidadParadas - 1) * 15

    /*
     * Devuelve una lista con una parada desplazada hacia arriba o abajo.
     * Si el movimiento sale de los límites, conserva el orden anterior.
     */
    fun moverParada(paradas: List<Int>, indice: Int, desplazamiento: Int): List<Int> {
        val destino = indice + desplazamiento
        if (indice !in paradas.indices || destino !in paradas.indices) return paradas
        return paradas.toMutableList().apply { add(destino, removeAt(indice)) }
    }
}
