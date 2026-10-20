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

@Composable
fun CatalogoScreen(vm: PatrimonioViewModel, navegar: (String) -> Unit, soloFavoritos: Boolean = false) {
    var consulta by rememberSaveable { mutableStateOf("") }
    var abiertos by rememberSaveable { mutableStateOf(false) }
    var tipo by rememberSaveable { mutableStateOf("Todos") }
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
            IconToggleButton(checked = id in vm.favoritos, onCheckedChange = { vm.alternarFavorito(id) }) {
                Icono(if (id in vm.favoritos) Simbolo.CHECK else Simbolo.FAVORITO,
                    if (id in vm.favoritos) "Quitar de favoritos" else "Guardar en favoritos")
            }
        }
        Etiqueta(recinto.estado.etiqueta)
        Pestanas(listOf("Información", "Historia", "Horarios", "Eventos", "Avisos"), pestana) { pestana = it }
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

@Composable
internal fun EventoCard(evento: Evento, recinto: String) {
    Seccion(evento.titulo) {
        Text(recinto, style = MaterialTheme.typography.labelLarge)
        DatoFila(Simbolo.CALENDARIO, "Fecha y hora", "${evento.fecha} · ${evento.hora}")
        Text(evento.descripcion)
        if (evento.cancelado) Aviso("Evento cancelado", error = true)
    }
}
