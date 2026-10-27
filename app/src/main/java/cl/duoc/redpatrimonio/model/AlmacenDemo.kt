/*
 * AlmacenDemo.kt conserva el estado general del prototipo en el teléfono.
 * Convierte recintos, eventos, rutas, visitas y disponibilidad a texto JSON y lo guarda
 * con SharedPreferences, un almacenamiento privado de pares clave y valor de Android.
 * PatrimonioViewModel llama a cargar al iniciarse y a guardar cuando modifica ese estado.
 * Los favoritos usados actualmente por la interfaz se consultan en Room. El campo
 * favoritos del JSON se conserva por compatibilidad con el formato anterior.
 * Para explicarlo: SharedPreferences guarda la demo; Room guarda los favoritos activos.
 */
package cl.duoc.redpatrimonio.model

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Almacenamiento de la demostración. No guarda contraseñas ni sustituye una base de datos. */
class AlmacenDemo(context: Context, nombre: String = "patrimonio_demo_v1") {
    private val preferencias = context.getSharedPreferences(nombre, Context.MODE_PRIVATE)

    /*
     * Lee el JSON guardado en la clave estado y reconstruye los objetos Kotlin.
     * Si no hay datos o el formato es incompatible, devuelve el catálogo inicial.
     */
    fun cargar(): EstadoDemo {
        val texto = preferencias.getString("estado", null) ?: return EstadoDemo()
        return try {
            val root = JSONObject(texto)
            /*
             * Esta versión corresponde al formato del JSON de la demo, no a la versión de Room.
             */
            require(root.getInt("version") == 1)
            val favoritos = root.getJSONObject("favoritos")
            EstadoDemo(
                recintos = root.getJSONArray("recintos").objetos().map { r ->
                    Recinto(r.getInt("id"), r.getString("nombre"), r.getString("tipo"),
                        r.getString("comuna"), r.getString("direccion"), r.getString("historia"),
                        r.getString("horario"), EstadoRecinto.valueOf(r.getString("estado")), r.getString("aviso"))
                },
                eventos = root.getJSONArray("eventos").objetos().map { e ->
                    Evento(e.getInt("id"), e.getInt("recintoId"), e.getString("titulo"),
                        e.getString("fecha"), e.getString("hora"), e.getString("descripcion"), e.getBoolean("cancelado"))
                },
                rutas = root.getJSONArray("rutas").objetos().map { r ->
                    RutaPatrimonial(r.getInt("id"), r.getString("nombre"), r.getJSONArray("paradas").enteros(),
                        r.getBoolean("sugerida"), r.getString("propietario"))
                },
                visitas = root.getJSONArray("visitas").objetos().map { v ->
                    Visita(v.getInt("id"), v.getInt("rutaId"), v.getString("fecha"), v.getString("hora"),
                        v.getInt("personas"), v.getString("encuentro"), v.getBoolean("requiereGuia"),
                        EstadoVisita.valueOf(v.getString("estado")), v.getBoolean("guiaAsignado"), v.getString("propietario"), v.optBoolean("guiaRechazado", false))
                },
                favoritos = favoritos.keys().asSequence().associateWith { favoritos.getJSONArray(it).enteros().toSet() },
                disponibilidadGuia = root.getJSONArray("disponibilidad").let { a ->
                    (0 until a.length()).map { a.getString(it) }.toSet()
                }
            )
        } catch (_: Exception) {
            // Datos de demostración incompatibles: se recupera el catálogo inicial.
            EstadoDemo()
        }
    }

    /*
     * Convierte EstadoDemo a JSON y lo guarda en SharedPreferences.
     * apply actualiza la preferencia en memoria y programa su escritura a disco.
     * Este método no escribe en la tabla de favoritos de Room.
     */
    fun guardar(estado: EstadoDemo) {
        val root = JSONObject().put("version", 1)
        root.put("recintos", JSONArray().apply { estado.recintos.forEach { r ->
            put(JSONObject().put("id", r.id).put("nombre", r.nombre).put("tipo", r.tipo)
                .put("comuna", r.comuna).put("direccion", r.direccion).put("historia", r.historia)
                .put("horario", r.horario).put("estado", r.estado.name).put("aviso", r.aviso))
        } })
        root.put("eventos", JSONArray().apply { estado.eventos.forEach { e ->
            put(JSONObject().put("id", e.id).put("recintoId", e.recintoId).put("titulo", e.titulo)
                .put("fecha", e.fecha).put("hora", e.hora).put("descripcion", e.descripcion).put("cancelado", e.cancelado))
        } })
        root.put("rutas", JSONArray().apply { estado.rutas.forEach { r ->
            put(JSONObject().put("id", r.id).put("nombre", r.nombre).put("paradas", JSONArray(r.paradas))
                .put("sugerida", r.sugerida).put("propietario", r.propietario))
        } })
        root.put("visitas", JSONArray().apply { estado.visitas.forEach { v ->
            put(JSONObject().put("id", v.id).put("rutaId", v.rutaId).put("fecha", v.fecha).put("hora", v.hora)
                .put("personas", v.personas).put("encuentro", v.encuentro).put("requiereGuia", v.requiereGuia)
                .put("estado", v.estado.name).put("guiaAsignado", v.guiaAsignado).put("propietario", v.propietario).put("guiaRechazado", v.guiaRechazado))
        } })
        root.put("favoritos", JSONObject().apply { estado.favoritos.forEach { (cuenta, ids) -> put(cuenta, JSONArray(ids.toList())) } })
        root.put("disponibilidad", JSONArray(estado.disponibilidadGuia.sorted()))
        preferencias.edit().putString("estado", root.toString()).apply()
    }

    /*
     * Funciones de extensión: convierten arreglos JSON a listas para simplificar la carga.
     */
    private fun JSONArray.objetos() = (0 until length()).map { getJSONObject(it) }
    private fun JSONArray.enteros() = (0 until length()).map { getInt(it) }
}
