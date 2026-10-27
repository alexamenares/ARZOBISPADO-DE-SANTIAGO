/*
 * CatalogoScreens.kt contiene catálogo, favoritos, mapa, ficha y listado de eventos.
 * CatalogoScreen reutiliza su diseño: soloFavoritos=true filtra por los IDs guardados en Room.
 * FichaScreen recibe un ID, muestra información y solicita guardar o quitar el favorito.
 * Los filtros y pestañas son estado de pantalla; el catálogo general lo entrega el ViewModel.
 * Para explicarlo: muestra y filtra los datos; las reglas y persistencia están en otras capas.
 */
package cl.duoc.redpatrimonio.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.redpatrimonio.model.*
import cl.duoc.redpatrimonio.navigation.Routes
import cl.duoc.redpatrimonio.viewmodel.PatrimonioViewModel
import java.time.LocalDateTime

/*
 * Mantiene búsqueda, apertura y tipo como estado local. ReglasPatrimonio combina filtros.
 * Con soloFavoritos=true parte de los recintos cuyos IDs están en vm.favoritos.
 */
@Composable
fun CatalogoScreen(vm: PatrimonioViewModel, navegar: (String) -> Unit, soloFavoritos: Boolean = false) {
    var consulta by rememberSaveable { mutableStateOf("") }
    var abiertos by rememberSaveable { mutableStateOf(false) }
    var tipo by rememberSaveable { mutableStateOf("Todos") }
    /*
     * El filtro de favoritos usa el Set de IDs observado desde Room en el ViewModel.
     * La búsqueda se recalcula cuando cambian los filtros o los datos que lee Compose.
     */
    val base = vm.estado.recintos.filter { !soloFavoritos || it.id in vm.favoritos }
    val resultados = ReglasPatrimonio.buscar(base, consulta, abiertos, tipo)
    Pagina {
        if (!soloFavoritos) FilaAcceso("Explorar en el mapa", "Vista general de la red patrimonial", Simbolo.MAPA) { navegar(Routes.MAPA) }
        Campo(consulta, "Buscar por nombre, tipo o comuna", { consulta = it })
        FilterChip(selected = abiertos, onClick = { abiertos = !abiertos }, label = { Text("Solo abiertos") })
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(listOf("Todos") + vm.estado.recintos.map { it.tipo }.distinct()) { opcion ->
                FilterChip(selected = tipo == opcion, onClick = { tipo = opcion }, label = { Text(opcion) })
            }
        }
        Text("${resultados.size} recintos", style = MaterialTheme.typography.titleMedium)
        if (resultados.isEmpty()) Aviso(if (soloFavoritos && base.isEmpty())
            "Todavía no tienes favoritos. Abre una ficha y pulsa Guardar en favoritos."
            else "No encontramos recintos con esos filtros. Prueba otra búsqueda.")
        resultados.forEach { recinto -> RecintoCard(recinto, recinto.id in vm.favoritos) { navegar(Routes.ficha(recinto.id)) } }
    }
}

/*
 * Presenta el mapa dibujado y una lista de fichas. No usa ubicación real del teléfono.
 */
@Composable
fun MapaScreen(vm: PatrimonioViewModel, navegar: (String) -> Unit) {
    Pagina {
        Seccion("Mapa de referencia") {
            MapaIlustrativo(Modifier.fillMaxWidth().height(300.dp))
            Aviso("Ilustración del prototipo. Los marcadores no representan posiciones reales; GPS y cálculo geográfico corresponden a una etapa posterior.")
        }
        Text("Recintos de la red", style = MaterialTheme.typography.titleLarge)
        vm.estado.recintos.forEach { recinto -> RecintoCard(recinto, recinto.id in vm.favoritos) { navegar(Routes.ficha(recinto.id)) } }
    }
}

/*
 * Busca el recinto por ID y cambia el contenido según la pestaña seleccionada.
 * El favorito se modifica con vm.alternarFavorito; la edición depende de vm.puedeEditar.
 */
@Composable
fun FichaScreen(id: Int, vm: PatrimonioViewModel, navegar: (String) -> Unit) {
    val recinto = vm.recinto(id)
    var pestana by rememberSaveable(id) { mutableStateOf(0) }
    Pagina {
        if (recinto == null) { Aviso("El recinto no está disponible."); return@Pagina }
        IlustracionRecinto(Modifier.fillMaxWidth().height(210.dp).clip(RoundedCornerShape(16.dp)), id)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Encabezado(recinto.nombre, "${recinto.tipo} · ${recinto.comuna}")
            }
            /*
             * Pulsar solicita alternar el favorito. Room emite los nuevos IDs y el ViewModel
             * actualiza vm.favoritos; entonces Compose vuelve a dibujar el estado del icono.
             */
            IconToggleButton(checked = id in vm.favoritos, onCheckedChange = { vm.alternarFavorito(id) }) {
                Icono(if (id in vm.favoritos) Simbolo.CHECK else Simbolo.FAVORITO,
                    if (id in vm.favoritos) "Quitar de favoritos" else "Guardar en favoritos")
            }
        }
        Etiqueta(recinto.estado.etiqueta)
        Pestanas(listOf("Información", "Historia", "Horarios", "Eventos", "Avisos"), pestana) { pestana = it }
        /*
         * Cada número muestra una sección distinta de la misma ficha; no es otra ruta del NavHost.
         */
        when (pestana) {
            0 -> Seccion("Sobre este recinto") {
                DatoFila(Simbolo.MAPA, "Ubicación", recinto.direccion)
                DatoFila(Simbolo.RELOJ, "Horario", recinto.horario)
                Aviso("Dirección y contenido de demostración. Ilustración referencial.")
            }
            1 -> Seccion("Historia y patrimonio") { Text(recinto.historia) }
            2 -> Seccion("Horarios de visita") { DatoFila(Simbolo.RELOJ, "Atención", recinto.horario) }
            3 -> {
                val eventos = vm.estado.eventos.filter { it.recintoId == id }
                if (eventos.isEmpty()) Aviso("Este recinto todavía no tiene eventos publicados.")
                eventos.forEach { EventoCard(it, recinto.nombre) }
            }
            4 -> Seccion("Avisos del recinto") { Text(recinto.aviso.ifBlank { "No hay avisos especiales." }) }
        }
        Accion("Crear ruta desde aquí", dorado = true) { navegar(Routes.editarRuta(semilla = id)) }
        if (vm.puedeEditar(id)) Accion("Editar este recinto") { navegar(Routes.editarRecinto(id)) }
    }
}

/*
 * Lista eventos próximos o, si se activa el filtro, también cancelados y anteriores.
 * Ordena por fecha y hora antes de dibujar las tarjetas.
 */
@Composable
fun EventosScreen(vm: PatrimonioViewModel) {
    var mostrarPasados by rememberSaveable { mutableStateOf(false) }
    val eventos = vm.estado.eventos.filter {
        mostrarPasados || (!it.cancelado && ReglasPatrimonio.fechaHora(it.fecha, it.hora)?.isAfter(LocalDateTime.now()) == true)
    }.sortedWith(compareBy({ it.fecha }, { it.hora }))
    Pagina {
        FilterChip(selected = mostrarPasados, onClick = { mostrarPasados = !mostrarPasados },
            label = { Text("Incluir cancelados y anteriores") })
        if (eventos.isEmpty()) Aviso("No hay eventos próximos publicados.")
        eventos.forEach { EventoCard(it, vm.recinto(it.recintoId)?.nombre.orEmpty()) }
    }
}

/*
 * Tarjeta de actividad con recinto, fecha, descripción y aviso de cancelación.
 */
@Composable
internal fun EventoCard(evento: Evento, recinto: String) {
    Seccion(evento.titulo) {
        Text(recinto, style = MaterialTheme.typography.labelLarge)
        DatoFila(Simbolo.CALENDARIO, "Fecha y hora", "${evento.fecha} · ${evento.hora}")
        Text(evento.descripcion)
        if (evento.cancelado) Aviso("Evento cancelado", error = true)
    }
}
