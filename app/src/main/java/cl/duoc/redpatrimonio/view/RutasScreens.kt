package cl.duoc.redpatrimonio.view

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cl.duoc.redpatrimonio.model.ReglasPatrimonio
import cl.duoc.redpatrimonio.navigation.Routes
import cl.duoc.redpatrimonio.viewmodel.PatrimonioViewModel

@Composable
fun RutasScreen(vm: PatrimonioViewModel, navegar: (String) -> Unit) {
    Pagina {
        Encabezado("Mis rutas", "Diseña un recorrido y descubre la red")
        Accion("Crear nueva ruta", dorado = true) { navegar(Routes.editarRuta()) }
        vm.rutasVisibles.forEach { ruta ->
            FilaAcceso(ruta.nombre,
                "${if (ruta.sugerida) "Sugerida" else "Personal"} · ${ruta.paradas.size} paradas · ${ReglasPatrimonio.minutosEstimados(ruta.paradas.size)} min",
                Simbolo.RUTA) { navegar(Routes.ruta(ruta.id)) }
        }
    }
}

@Composable
fun EditarRutaScreen(id: Int, semilla: Int, vm: PatrimonioViewModel, volver: () -> Unit) {
    val original = vm.ruta(id)
    var nombre by rememberSaveable(id) { mutableStateOf(original?.nombre.orEmpty()) }
    var paradas by rememberSaveable(id, semilla) { mutableStateOf(ArrayList(original?.paradas ?: listOf(semilla).filter { vm.recinto(it) != null })) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var paso by rememberSaveable(id) { mutableStateOf(0) }
    BackHandler(enabled = paso > 0) { paso--; error = null }
    Pagina {
        if (id != -1 && (original == null || !vm.puedeEditarRuta(original))) {
            Aviso("Esta ruta no está disponible para edición."); return@Pagina
        }
        Pasos(paso, listOf("Nombre", "Recintos", "Orden"))
        if (paso == 0) {
            Encabezado("Tu próximo recorrido", "Dale un nombre a esta ruta patrimonial")
            Campo(nombre, "Nombre de la ruta", { nombre = it })
        }
        if (paso == 1) {
        Encabezado("Selecciona tus recintos", "${paradas.size} seleccionados · mínimo 2, máximo 6")
        vm.estado.recintos.forEach { recinto ->
            Seccion("") {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = recinto.id in paradas, enabled = recinto.id in paradas || paradas.size < 6, onCheckedChange = { seleccionado ->
                    paradas = ArrayList(if (seleccionado) (paradas + recinto.id).distinct() else paradas - recinto.id)
                })
                Column(Modifier.weight(1f)) {
                    Text(recinto.nombre)
                    Text(recinto.estado.etiqueta, style = MaterialTheme.typography.bodySmall)
                }
            }
            }
        }
        }
        if (paso == 2 && paradas.isNotEmpty()) Seccion("Orden de las paradas") {
            paradas.forEachIndexed { indice, recintoId ->
                Column {
                    Text("${indice + 1}. ${vm.recinto(recintoId)?.nombre.orEmpty()}")
                    Row {
                        TextButton(enabled = indice > 0, onClick = { paradas = ArrayList(ReglasPatrimonio.moverParada(paradas, indice, -1)) }) { Text("Subir") }
                        TextButton(enabled = indice < paradas.lastIndex, onClick = { paradas = ArrayList(ReglasPatrimonio.moverParada(paradas, indice, 1)) }) { Text("Bajar") }
                    }
                }
            }
        }
        error?.let { Aviso(it, error = true) }
        if (paso < 2) Accion("Continuar", dorado = true) {
            error = if (paso == 0 && nombre.trim().length !in 3..80) "Escribe un nombre de entre 3 y 80 caracteres."
                else if (paso == 1) ReglasPatrimonio.validarRuta(nombre, paradas, vm.estado.recintos.map { it.id }.toSet()) else null
            if (error == null) paso++
        } else Accion("Guardar ruta", dorado = true) {
            error = vm.guardarRuta(id.takeIf { it != -1 }, nombre, paradas)
            if (error == null) volver()
        }
        if (paso > 0) TextButton(onClick = { paso--; error = null }) { Text("Paso anterior") }
    }
}

@Composable
fun RutaScreen(id: Int, vm: PatrimonioViewModel, navegar: (String) -> Unit, volver: () -> Unit) {
    val ruta = vm.rutasVisibles.find { it.id == id }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmar by rememberSaveable { mutableStateOf(false) }
    var recorrido by rememberSaveable(id) { mutableStateOf(false) }
    var indice by rememberSaveable(id) { mutableStateOf(0) }
    var finalizado by rememberSaveable(id) { mutableStateOf(false) }
    if (confirmar) Confirmar("Eliminar ruta", "Se eliminará esta ruta personal.", { confirmar = false }) {
        confirmar = false
        error = vm.eliminarRuta(id)
        if (error == null) volver()
    }
    Pagina {
        if (ruta == null) { Aviso("La ruta no está disponible."); return@Pagina }
        MapaIlustrativo(Modifier.fillMaxWidth().height(200.dp))
        Seccion(ruta.nombre) {
            Text("${ruta.paradas.size} paradas · ${ReglasPatrimonio.minutosEstimados(ruta.paradas.size)} minutos estimados")
            Text("Estimación académica: 25 minutos por recinto y 15 entre paradas. No calcula distancias reales.", style = MaterialTheme.typography.bodySmall)
        }
        if (!recorrido) ruta.paradas.forEachIndexed { i, recintoId ->
            vm.recinto(recintoId)?.let { recinto ->
                Text("Parada ${i + 1}", style = MaterialTheme.typography.labelLarge)
                RecintoCard(recinto, recintoId in vm.favoritos) { navegar(Routes.ficha(recintoId)) }
            }
        }
        if (recorrido) Seccion("Recorrido de demostración") {
            Text("Parada ${indice + 1}: ${vm.recinto(ruta.paradas.getOrNull(indice) ?: -1)?.nombre.orEmpty()}")
            ruta.paradas.getOrNull(indice + 1)?.let { siguiente ->
                DatoFila(Simbolo.RUTA, "Próxima parada", vm.recinto(siguiente)?.nombre.orEmpty())
            }
            Text("Avanza manualmente para probar el flujo; no utiliza GPS.")
            Accion(if (indice < ruta.paradas.lastIndex) "Siguiente parada" else "Finalizar recorrido") {
                if (indice < ruta.paradas.lastIndex) indice++ else { recorrido = false; finalizado = true }
            }
            TextButton(onClick = { recorrido = false }) { Text("Salir del recorrido") }
        } else Accion("Iniciar recorrido de prueba") { indice = 0; finalizado = false; recorrido = true }
        if (finalizado) Aviso("Recorrido de prueba completado.")
        Accion("Planificar visita", dorado = true) { navegar(Routes.nuevaVisita(id)) }
        if (vm.puedeEditarRuta(ruta)) {
            Accion("Editar ruta") { navegar(Routes.editarRuta(id)) }
            TextButton(onClick = { confirmar = true }) { Text("Eliminar ruta") }
        }
        error?.let { Aviso(it, error = true) }
    }
}
