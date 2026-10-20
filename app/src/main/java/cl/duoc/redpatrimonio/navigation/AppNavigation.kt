package cl.duoc.redpatrimonio.navigation

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.*
import cl.duoc.redpatrimonio.model.Rol
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.*
import cl.duoc.redpatrimonio.view.*
import cl.duoc.redpatrimonio.viewmodel.LoginViewModel
import cl.duoc.redpatrimonio.viewmodel.PatrimonioViewModel

@Composable
fun AppNavigation() {
    val vm: PatrimonioViewModel = viewModel()
    val login: LoginViewModel = viewModel()
    // La sesión es temporal. Al terminar el proceso se vuelve a solicitar acceso.
    if (vm.rol == null) {
        LoginScreen(onLoginSuccess = { rol -> vm.iniciarSesion(rol); login.limpiar() }, loginViewModel = login)
    } else {
        key(vm.rol) { NavegacionAutenticada(vm) { login.limpiar(); vm.cerrarSesion() } }
    }
}

@Composable
private fun NavegacionAutenticada(vm: PatrimonioViewModel, salir: () -> Unit) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val actual = entry?.destination?.route ?: Routes.HOME
    val botones = when (vm.rol) {
        Rol.GUIA -> listOf(Triple(Routes.HOME, "Inicio", Simbolo.INICIO), Triple(Routes.VISITAS, "Recorridos", Simbolo.RUTA), Triple(Routes.GUIA, "Agenda", Simbolo.CALENDARIO), Triple(Routes.PERFIL, "Perfil", Simbolo.PERFIL))
        Rol.ADMINISTRADOR -> listOf(Triple(Routes.HOME, "Panel", Simbolo.INICIO), Triple(Routes.GESTION, "Recintos", Simbolo.EDIFICIO), Triple(Routes.VISITAS, "Visitas", Simbolo.CALENDARIO), Triple(Routes.PERFIL, "Perfil", Simbolo.PERFIL))
        Rol.ENCARGADO -> listOf(Triple(Routes.HOME, "Inicio", Simbolo.INICIO), Triple(Routes.GESTION, "Gestión", Simbolo.EDIFICIO), Triple(Routes.PERFIL, "Perfil", Simbolo.PERFIL))
        else -> listOf(Triple(Routes.HOME, "Inicio", Simbolo.INICIO), Triple(Routes.CATALOGO, "Explorar", Simbolo.MAPA), Triple(Routes.RUTAS, "Rutas", Simbolo.RUTA), Triple(Routes.VISITAS, "Visitas", Simbolo.CALENDARIO), Triple(Routes.PERFIL, "Perfil", Simbolo.PERFIL))
    }
    val principales = botones.map { it.first }
    val dispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val navegar: (String) -> Unit = { destino ->
        nav.navigate(destino) {
            launchSingleTop = true
            if (destino in principales) popUpTo(Routes.HOME)
        }
    }
    val volver: () -> Unit = { nav.popBackStack(); Unit }
    val titulo = when (actual) {
        Routes.HOME -> "Red Patrimonial"
        Routes.CATALOGO -> "Catálogo de recintos"
        Routes.MAPA -> "Mapa de referencia"
        Routes.FICHA -> "Ficha patrimonial"
        Routes.EVENTOS -> "Eventos culturales"
        Routes.FAVORITOS -> "Mis favoritos"
        Routes.RUTAS -> "Rutas patrimoniales"
        Routes.RUTA -> "Detalle de ruta"
        Routes.EDITAR_RUTA -> "Editor de ruta"
        Routes.VISITAS -> "Visitas y solicitudes"
        Routes.NUEVA_VISITA -> "Planificar visita"
        Routes.GUIA -> "Disponibilidad del guía"
        Routes.GESTION -> "Gestión de la red"
        Routes.EDITAR_RECINTO -> "Editar recinto"
        Routes.EDITAR_EVENTO -> "Editar evento"
        else -> "Mi perfil"
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            val fondo = if (actual == Routes.HOME || vm.rol == Rol.GUIA) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.primary
            val texto = if (actual == Routes.HOME || vm.rol == Rol.GUIA) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onPrimary
            Surface(color = fondo, contentColor = texto) {
                if (actual == Routes.HOME) Spacer(Modifier.fillMaxWidth().statusBarsPadding())
                else Row(Modifier.fillMaxWidth().statusBarsPadding().padding(end = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { dispatcher?.onBackPressed() ?: volver() }) { Icono(Simbolo.ATRAS, "Volver") }
                    Text(titulo, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                botones.forEach { (destino, etiqueta, simbolo) ->
                    val seleccion = when (actual) {
                        Routes.FICHA, Routes.MAPA, Routes.EVENTOS -> Routes.CATALOGO
                        Routes.FAVORITOS -> Routes.PERFIL
                        Routes.RUTA, Routes.EDITAR_RUTA -> Routes.RUTAS
                        Routes.NUEVA_VISITA -> Routes.VISITAS
                        Routes.EDITAR_RECINTO, Routes.EDITAR_EVENTO -> Routes.GESTION
                        else -> actual
                    }
                    NavigationBarItem(selected = seleccion == destino, onClick = { navegar(destino) },
                        icon = { Icono(simbolo) }, label = { Text(etiqueta, style = MaterialTheme.typography.labelSmall) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer))
                }
            }
        }
    ) { padding ->
        NavHost(navController = nav, startDestination = Routes.HOME, modifier = Modifier.padding(padding).consumeWindowInsets(padding)) {
            composable(Routes.HOME) { HomeScreen(vm, navegar) }
            composable(Routes.CATALOGO) { CatalogoScreen(vm, navegar) }
            composable(Routes.MAPA) { MapaScreen(vm, navegar) }
            composable(Routes.FAVORITOS) { CatalogoScreen(vm, navegar, soloFavoritos = true) }
            composable(Routes.FICHA) { FichaScreen(it.numero("id"), vm, navegar) }
            composable(Routes.EVENTOS) { EventosScreen(vm) }
            composable(Routes.RUTAS) { RutasScreen(vm, navegar) }
            composable(Routes.RUTA) { RutaScreen(it.numero("id"), vm, navegar, volver) }
            composable(Routes.EDITAR_RUTA) { EditarRutaScreen(it.numero("id"), it.numero("semilla"), vm, volver) }
            composable(Routes.VISITAS) { VisitasScreen(vm, navegar) }
            composable(Routes.NUEVA_VISITA) { NuevaVisitaScreen(it.numero("id"), vm) { navegar(Routes.VISITAS) } }
            composable(Routes.GUIA) { GuiaScreen(vm, navegar) }
            composable(Routes.GESTION) { GestionScreen(vm, navegar) }
            composable(Routes.EDITAR_RECINTO) { EditarRecintoScreen(it.numero("id"), vm, volver) }
            composable(Routes.EDITAR_EVENTO) { EditarEventoScreen(it.numero("recintoId"), it.numero("eventoId"), vm, volver) }
            composable(Routes.PERFIL) { PerfilScreen(vm, navegar, salir) }
        }
    }
}

private fun NavBackStackEntry.numero(clave: String): Int = arguments?.getString(clave)?.toIntOrNull() ?: -1
