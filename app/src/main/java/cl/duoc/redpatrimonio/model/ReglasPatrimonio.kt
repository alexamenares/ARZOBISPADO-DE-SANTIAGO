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
    fun normalizar(texto: String): String = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
        .replace("\\p{M}".toRegex(), "").lowercase(Locale.ROOT)

    fun buscar(recintos: List<Recinto>, consulta: String, soloAbiertos: Boolean, tipo: String): List<Recinto> {
        val texto = normalizar(consulta)
        return recintos.filter {
            (!soloAbiertos || it.estado == EstadoRecinto.ABIERTO) &&
                (tipo == "Todos" || it.tipo == tipo) &&
                normalizar("${it.nombre} ${it.tipo} ${it.comuna}").contains(texto)
        }
    }

    fun validarRuta(nombre: String, paradas: List<Int>, idsValidos: Set<Int>): String? = when {
        nombre.trim().length !in 3..80 -> "Escribe un nombre de entre 3 y 80 caracteres."
        paradas.size !in 2..6 -> "Selecciona entre 2 y 6 recintos."
        paradas.distinct().size != paradas.size -> "Cada recinto puede aparecer una sola vez."
        paradas.any { it !in idsValidos } -> "La ruta contiene un recinto que ya no está disponible."
        else -> null
    }

    fun fechaHora(fecha: String, hora: String): LocalDateTime? = try {
        if (!fecha.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) || !hora.matches(Regex("\\d{2}:\\d{2}"))) null
        else LocalDateTime.of(LocalDate.parse(fecha),
            LocalTime.parse(hora, DateTimeFormatter.ofPattern("HH:mm").withResolverStyle(ResolverStyle.STRICT)))
    } catch (_: java.time.DateTimeException) { null }

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

    fun moverParada(paradas: List<Int>, indice: Int, desplazamiento: Int): List<Int> {
        val destino = indice + desplazamiento
        if (indice !in paradas.indices || destino !in paradas.indices) return paradas
        return paradas.toMutableList().apply { add(destino, removeAt(indice)) }
    }
}
