/*
 * HomeScreen.kt reúne los inicios de visitante/docente, guía y gestión.
 * HomeScreen elige una presentación según el rol activo en PatrimonioViewModel.
 * Muestra resúmenes y accesos mediante navegar y las constantes de Routes.
 * El botón QR conduce a QrScannerScreen; Google abre la cámara al pulsar Escanear QR allí.
 * MapaIlustrativo es un dibujo de referencia: no obtiene GPS ni calcula distancias.
 */
package cl.duoc.redpatrimonio.view

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import cl.duoc.redpatrimonio.model.*
import cl.duoc.redpatrimonio.navigation.Routes
import cl.duoc.redpatrimonio.ui.theme.*
import cl.duoc.redpatrimonio.viewmodel.PatrimonioViewModel
import java.time.LocalDate

/*
 * Elige el inicio adecuado. Visitante y docente comparten el catálogo público.
 */
@Composable
fun HomeScreen(vm: PatrimonioViewModel, navegar: (String) -> Unit) {
    when(vm.rol) {
        Rol.GUIA -> InicioGuia(vm, navegar)
        Rol.ADMINISTRADOR, Rol.ENCARGADO -> InicioGestion(vm, navegar)
        else -> InicioVisitante(vm, navegar)
    }
}

/*
 * Cabecera con saludo y botón que solicita navegar hacia Perfil.
 */
@Composable
private fun Saludo(subtitulo: String, nombre: String, navegar: (String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(subtitulo, style = MaterialTheme.typography.bodySmall, color = PatrimonioSecundario)
            Text(nombre, style = MaterialTheme.typography.headlineSmall)
        }
        FilledTonalIconButton(onClick = { navegar(Routes.PERFIL) },
            colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = PatrimonioTerracota, contentColor = PatrimonioBlanco)) {
            Icono(Simbolo.PERFIL, "Abrir mi perfil")
        }
    }
}

/*
 * Accesos al catálogo, QR, eventos, rutas y mapa de referencia.
 * Recibe el estado del ViewModel y entrega las rutas al callback navegar.
 */
@Composable
private fun InicioVisitante(vm: PatrimonioViewModel, navegar: (String) -> Unit) {
    Pagina {
        Saludo("Bienvenido a la red", if(vm.rol==Rol.DOCENTE) "Explora y aprende" else "Descubre el patrimonio", navegar)
        OutlinedButton(onClick = { navegar(Routes.CATALOGO) }, modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(14.dp)) {
            Icono(Simbolo.BUSCAR, color = PatrimonioSecundario)
            Spacer(Modifier.width(8.dp))
            Text("Buscar recintos, lugares...", modifier = Modifier.weight(1f), color = PatrimonioSecundario, style = MaterialTheme.typography.bodyMedium)
        }

        // Abre la cámara para leer el código QR asociado a un recinto patrimonial.
        Button(
            onClick = { navegar(Routes.QR_SCANNER) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PatrimonioTerracota,
                contentColor = PatrimonioBlanco
            )
        ) {
            // Usamos el ícono de búsqueda como representación visual de la acción.
            Icono(Simbolo.BUSCAR, "Escanear código QR")
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Escanear código QR",
                modifier = Modifier.weight(1f)
            )
        }

        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(Brush.horizontalGradient(listOf(PatrimonioTerracota, PatrimonioDorado))).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Vive nuestro patrimonio", style = MaterialTheme.typography.titleMedium, color = PatrimonioBlanco)
            Text("Encuentros, cultura e historia", style = MaterialTheme.typography.bodySmall, color = PatrimonioBlanco)
            TextButton(onClick = { navegar(Routes.EVENTOS) },
                colors = ButtonDefaults.textButtonColors(contentColor = PatrimonioBlanco)) { Text("Explorar eventos →") }
        }
        TituloSeccion("Recintos destacados", onClick = { navegar(Routes.CATALOGO) })
        /*
         * Lista horizontal de tarjetas con clave estable por ID. Pulsar una tarjeta
         * entrega Routes.ficha(id) a la navegación; la vista no maneja directamente el NavController.
         */
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(vm.estado.recintos, key = { it.id }) { recinto ->
                Card(onClick = { navegar(Routes.ficha(recinto.id)) }, modifier = Modifier.width(164.dp),
                    shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = PatrimonioBlanco)) {
                    IlustracionRecinto(Modifier.fillMaxWidth().height(104.dp),recinto.id)
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(recinto.tipo, style = MaterialTheme.typography.labelSmall, color = PatrimonioTerracota)
                        Text(recinto.nombre, style = MaterialTheme.typography.titleSmall, minLines = 2)
                        Text(recinto.comuna, style = MaterialTheme.typography.bodySmall, color = PatrimonioSecundario)
                    }
                }
            }
        }
        TituloSeccion("Rutas para descubrir", "Ver todas", { navegar(Routes.RUTAS) })
        vm.rutasVisibles.take(2).forEach { ruta ->
            FilaAcceso(ruta.nombre, "${ruta.paradas.size} paradas · ${ReglasPatrimonio.minutosEstimados(ruta.paradas.size)} min estimados", Simbolo.RUTA) { navegar(Routes.ruta(ruta.id)) }
        }
        TituloSeccion("Próximos eventos", onClick = { navegar(Routes.EVENTOS) })
        /*
         * Solo se destacan hasta dos eventos futuros y no cancelados, ordenados por fecha y hora.
         */
        val eventos = vm.estado.eventos.filter { !it.cancelado && ReglasPatrimonio.fechaHora(it.fecha, it.hora)?.isAfter(java.time.LocalDateTime.now()) == true }.sortedWith(compareBy({ it.fecha }, { it.hora })).take(2)
        if(eventos.isEmpty()) Aviso("Pronto encontrarás nuevas actividades.")
        eventos.forEach { EventoCard(it,vm.recinto(it.recintoId)?.nombre.orEmpty()) }
        FilaAcceso("Mapa patrimonial", "Explora el esquema de la red", Simbolo.MAPA) { navegar(Routes.MAPA) }
    }
}

/*
 * Resume solicitudes con guía y días publicados, y ofrece acceso a la agenda.
 */
@Composable
private fun InicioGuia(vm: PatrimonioViewModel, navegar: (String) -> Unit) {
    val visitas=vm.visitasVisibles.filter { it.estado != EstadoVisita.CANCELADA }
    Pagina {
        Saludo("Bienvenido a tu panel", "Hola, guía", navegar)
        IlustracionRecinto(Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(12.dp)))
        Text("Resumen", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Metrica("Solicitudes",visitas.size,Simbolo.CALENDARIO,Modifier.weight(1f))
            Metrica("Aceptadas",visitas.count { it.guiaAsignado },Simbolo.CHECK,Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Metrica("Por aceptar",visitas.count { it.estado == EstadoVisita.PENDIENTE && !it.guiaAsignado && !it.guiaRechazado },Simbolo.RELOJ,Modifier.weight(1f))
            Metrica("Días disponibles",vm.estado.disponibilidadGuia.count { it >= LocalDate.now().toString() },Simbolo.LISTA,Modifier.weight(1f))
        }
        TituloSeccion("Próximos recorridos", onClick = { navegar(Routes.VISITAS) })
        val proximas=visitas.filter { it.fecha >= LocalDate.now().toString() }.sortedBy { it.fecha }.take(2)
        if(proximas.isEmpty()) Aviso("Todavía no hay solicitudes. Puedes preparar tu disponibilidad.")
        proximas.forEach { visita -> FilaAcceso(vm.ruta(visita.rutaId)?.nombre.orEmpty(), "${visita.fecha} · ${visita.hora} · ${visita.personas} personas",Simbolo.CALENDARIO) { navegar(Routes.VISITAS) } }
        FilaAcceso("Gestionar disponibilidad", "Actualiza los días en que puedes guiar", Simbolo.CALENDARIO) { navegar(Routes.GUIA) }
    }
}

/*
 * Muestra métricas y accesos para los recintos que el rol tiene permitido editar.
 */
@Composable
private fun InicioGestion(vm: PatrimonioViewModel, navegar: (String) -> Unit) {
    val recintos=vm.estado.recintos.filter { vm.puedeEditar(it.id) }
    Pagina {
        Saludo("Sistema de gestión",if(vm.rol==Rol.ADMINISTRADOR) "Administración" else "Mi recinto",navegar)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Metrica("Recintos",recintos.size,Simbolo.EDIFICIO,Modifier.weight(1f))
            Metrica("Eventos",vm.estado.eventos.count { e -> recintos.any { it.id == e.recintoId } && !e.cancelado },Simbolo.CALENDARIO,Modifier.weight(1f))
        }
        if(vm.rol==Rol.ADMINISTRADOR) Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Metrica("Pendientes",vm.estado.visitas.count { it.estado==EstadoVisita.PENDIENTE },Simbolo.RELOJ,Modifier.weight(1f))
            Metrica("Confirmadas",vm.estado.visitas.count { it.estado==EstadoVisita.CONFIRMADA },Simbolo.CHECK,Modifier.weight(1f))
        }
        TituloSeccion("Acciones rápidas")
        FilaAcceso("Gestionar recintos", "Fichas, estado, horarios y avisos",Simbolo.EDIFICIO) { navegar(Routes.GESTION) }
        FilaAcceso("Eventos y publicaciones", "Revisa y actualiza las actividades",Simbolo.CALENDARIO) { navegar(Routes.GESTION) }
        if(vm.rol==Rol.ADMINISTRADOR) FilaAcceso("Coordinar visitas", "Solicitudes y asignaciones de guía",Simbolo.LISTA) { navegar(Routes.VISITAS) }
        TituloSeccion("Estado de la red")
        recintos.forEach { RecintoCard(it,false) { navegar(Routes.editarRecinto(it.id)) } }
    }
}

/*
 * Tarjeta visual reutilizable con título, cantidad e icono; no calcula las cifras.
 */
@Composable
private fun Metrica(titulo: String, cantidad: Int, icono: Simbolo, modifier: Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = PatrimonioBlanco), shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icono(icono,color=PatrimonioTerracota)
            Text(cantidad.toString(),style=MaterialTheme.typography.headlineMedium)
            Text(titulo,style=MaterialTheme.typography.bodySmall,color=PatrimonioSecundario)
        }
    }
}

/** Dibujo de referencia: no solicita GPS ni representa ubicaciones reales. */
@Composable
internal fun MapaIlustrativo(modifier: Modifier = Modifier) {
    val fondo = Color(0xFFE8E3D9)
    val calles = Color(0xFFFAF8F4)
    val rio = Color(0xFFD4D9CA)
    val pin = MaterialTheme.colorScheme.primary
    val centro = MaterialTheme.colorScheme.onPrimary
    /*
     * Canvas ofrece coordenadas y operaciones de dibujo. Los puntos son ilustrativos,
     * por lo que no representan posiciones GPS ni se consultan mapas de Internet.
     */
    Canvas(modifier) {
        drawRect(fondo)
        val w = size.width
        val h = size.height
        drawPath(Path().apply {
            moveTo(0f, h * .7f)
            cubicTo(w * .3f, h, w * .6f, 0f, w, h * .2f)
            lineTo(w, h * .4f)
            cubicTo(w * .6f, h * .2f, w * .3f, h * 1.2f, 0f, h * .9f)
            close()
        }, rio)
        for (i in -2..6) {
            drawLine(calles, Offset(w * i / 5f, 0f), Offset(w * i / 5f + w * .3f, h), 9.dp.toPx())
        }
        for (i in 1..3) {
            drawLine(calles, Offset(0f, h * i / 4f), Offset(w, h * i / 4f - h * .2f), 7.dp.toPx())
        }
        listOf(.23f to .35f, .53f to .6f, .77f to .32f).forEach { (x, y) ->
            val center = Offset(w * x, h * y)
            val r = 12.dp.toPx()
            drawPath(Path().apply {
                moveTo(center.x - r * .75f, center.y + r * .4f)
                lineTo(center.x, center.y + r * 1.8f)
                lineTo(center.x + r * .75f, center.y + r * .4f)
                close()
            }, pin)
            drawCircle(pin, r, center)
            drawCircle(centro, r * .32f, center)
        }
    }
}

