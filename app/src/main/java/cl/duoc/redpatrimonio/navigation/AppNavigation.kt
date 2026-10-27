/*
 * AppNavigation.kt conecta las pantallas y organiza la navegación por rol.
 * AppNavigation muestra el login si no hay rol activo. NavegacionAutenticada crea el NavHost.
 * Scaffold coloca las barras superior e inferior alrededor del contenido de cada destino.
 * NavController administra la pila de pantallas; Routes centraliza las direcciones.
 * El resultado del QR se verifica aquí y luego se abre la ficha correspondiente.
 * Para explicarlo: este archivo decide qué pantalla mostrar y cómo llegar a otra.
 */
package cl.duoc.redpatrimonio.navigation

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cl.duoc.redpatrimonio.model.Rol
import cl.duoc.redpatrimonio.view.CatalogoScreen
import cl.duoc.redpatrimonio.view.EditarEventoScreen
import cl.duoc.redpatrimonio.view.EditarRecintoScreen
import cl.duoc.redpatrimonio.view.EditarRutaScreen
import cl.duoc.redpatrimonio.view.EventosScreen
import cl.duoc.redpatrimonio.view.FichaScreen
import cl.duoc.redpatrimonio.view.GestionScreen
import cl.duoc.redpatrimonio.view.GuiaScreen
import cl.duoc.redpatrimonio.view.HomeScreen
import cl.duoc.redpatrimonio.view.Icono
import cl.duoc.redpatrimonio.view.LoginScreen
import cl.duoc.redpatrimonio.view.MapaScreen
import cl.duoc.redpatrimonio.view.NuevaVisitaScreen
import cl.duoc.redpatrimonio.view.PerfilScreen
import cl.duoc.redpatrimonio.view.QrScannerScreen
import cl.duoc.redpatrimonio.view.RutaScreen
import cl.duoc.redpatrimonio.view.RutasScreen
import cl.duoc.redpatrimonio.view.Simbolo
import cl.duoc.redpatrimonio.view.VisitasScreen
import cl.duoc.redpatrimonio.viewmodel.LoginViewModel
import cl.duoc.redpatrimonio.viewmodel.PatrimonioViewModel

/**
 * Decide qué parte de la aplicación se muestra:
 * el inicio de sesión o la navegación disponible para el usuario autenticado.
 */
@Composable
fun AppNavigation() {
    /*
     * viewModel obtiene la instancia vinculada al propietario actual. Las pantallas
     * reciben este mismo vm para compartir estado en toda la navegación autenticada.
     */
    val vm: PatrimonioViewModel = viewModel()
    val login: LoginViewModel = viewModel()

    // El rol no se persiste: al recrear el proceso de la app se solicita acceso otra vez.
    if (vm.rol == null) {
        LoginScreen(
            onLoginSuccess = { rol ->
                vm.iniciarSesion(rol)
                login.limpiar()
            },
            loginViewModel = login
        )
    } else {
        // Al cambiar de rol, se crea nuevamente la navegación correspondiente.
        key(vm.rol) {
            NavegacionAutenticada(vm) {
                login.limpiar()
                vm.cerrarSesion()
            }
        }
    }
}

/**
 * Contiene las pantallas, barra superior y menú inferior
 * que se muestran después de iniciar sesión.
 */
@Composable
private fun NavegacionAutenticada(
    vm: PatrimonioViewModel,
    salir: () -> Unit
) {
    /*
     * remember conserva el controlador entre recomposiciones. La entrada actual se
     * observa como estado para actualizar título y botón seleccionado del menú inferior.
     */
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val actual = entry?.destination?.route ?: Routes.HOME

    // Cada rol dispone de accesos principales distintos.
    val botones = when (vm.rol) {
        Rol.GUIA -> listOf(
            Triple(Routes.HOME, "Inicio", Simbolo.INICIO),
            Triple(Routes.VISITAS, "Recorridos", Simbolo.RUTA),
            Triple(Routes.GUIA, "Agenda", Simbolo.CALENDARIO),
            Triple(Routes.PERFIL, "Perfil", Simbolo.PERFIL)
        )

        Rol.ADMINISTRADOR -> listOf(
            Triple(Routes.HOME, "Panel", Simbolo.INICIO),
            Triple(Routes.GESTION, "Recintos", Simbolo.EDIFICIO),
            Triple(Routes.VISITAS, "Visitas", Simbolo.CALENDARIO),
            Triple(Routes.PERFIL, "Perfil", Simbolo.PERFIL)
        )

        Rol.ENCARGADO -> listOf(
            Triple(Routes.HOME, "Inicio", Simbolo.INICIO),
            Triple(Routes.GESTION, "Gestión", Simbolo.EDIFICIO),
            Triple(Routes.PERFIL, "Perfil", Simbolo.PERFIL)
        )

        else -> listOf(
            Triple(Routes.HOME, "Inicio", Simbolo.INICIO),
            Triple(Routes.CATALOGO, "Explorar", Simbolo.MAPA),
            Triple(Routes.RUTAS, "Rutas", Simbolo.RUTA),
            Triple(Routes.VISITAS, "Visitas", Simbolo.CALENDARIO),
            Triple(Routes.PERFIL, "Perfil", Simbolo.PERFIL)
        )
    }

    val principales = botones.map { it.first }
    val dispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    /**
     * Navega a una pantalla. Si es una pantalla principal del menú inferior,
     * evita acumular copias repetidas de ella.
     */
    val navegar: (String) -> Unit = { destino ->
        nav.navigate(destino) {
            /*
             * Evita otra copia del destino si ya está arriba. popUpTo quita pantallas intermedias
             * al cambiar a una sección principal y mantiene Inicio como referencia de retorno.
             */
            launchSingleTop = true

            if (destino in principales) {
                popUpTo(Routes.HOME)
            }
        }
    }

    val volver: () -> Unit = {
        nav.popBackStack()
        Unit
    }

    // Título que aparece en la barra superior de cada pantalla.
    val titulo = when (actual) {
        Routes.HOME -> "Red Patrimonial"
        Routes.CATALOGO -> "Catálogo de recintos"
        Routes.MAPA -> "Mapa de referencia"
        Routes.QR_SCANNER -> "Escanear código QR"
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

    /*
     * topBar y bottomBar son áreas compartidas. El padding que recibe el contenido
     * evita dibujar las pantallas debajo de esas barras.
     */
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,

        topBar = {
            val fondo = if (actual == Routes.HOME || vm.rol == Rol.GUIA) {
                MaterialTheme.colorScheme.background
            } else {
                MaterialTheme.colorScheme.primary
            }

            val texto = if (actual == Routes.HOME || vm.rol == Rol.GUIA) {
                MaterialTheme.colorScheme.onBackground
            } else {
                MaterialTheme.colorScheme.onPrimary
            }

            Surface(
                color = fondo,
                contentColor = texto
            ) {
                if (actual == Routes.HOME) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                dispatcher?.onBackPressed() ?: volver()
                            }
                        ) {
                            Icono(Simbolo.ATRAS, "Volver")
                        }

                        Text(
                            text = titulo,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },

        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                botones.forEach { (destino, etiqueta, simbolo) ->
                    // Algunas pantallas pertenecen visualmente a una sección principal.
                    val seleccion = when (actual) {
                        Routes.FICHA,
                        Routes.MAPA,
                        Routes.EVENTOS -> Routes.CATALOGO

                        Routes.FAVORITOS -> Routes.PERFIL

                        Routes.RUTA,
                        Routes.EDITAR_RUTA -> Routes.RUTAS

                        Routes.NUEVA_VISITA -> Routes.VISITAS

                        Routes.EDITAR_RECINTO,
                        Routes.EDITAR_EVENTO -> Routes.GESTION

                        // El escáner se abrirá desde Inicio, por eso mantiene ese botón activo.
                        Routes.QR_SCANNER -> Routes.HOME

                        else -> actual
                    }

                    NavigationBarItem(
                        selected = seleccion == destino,
                        onClick = {
                            navegar(destino)
                        },
                        icon = {
                            Icono(simbolo)
                        },
                        label = {
                            Text(
                                text = etiqueta,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { padding ->
        /*
         * Cada composable registra una ruta y su pantalla. startDestination abre Inicio.
         * Los parámetros numéricos se recuperan con la función numero definida al final.
         */
        NavHost(
            navController = nav,
            startDestination = Routes.HOME,
            modifier = Modifier
                .padding(padding)
                .consumeWindowInsets(padding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(vm, navegar)
            }

            composable(Routes.CATALOGO) {
                CatalogoScreen(vm, navegar)
            }

            composable(Routes.MAPA) {
                MapaScreen(vm, navegar)
            }

            /**
             * El escáner entrega el id contenido en el QR.
             * Solo se abre la ficha si dicho recinto existe en la aplicación.
             */
            composable(Routes.QR_SCANNER) {
                /*
                 * La pantalla QR solo entrega un número. Aquí se consulta el ViewModel y se navega:
                 * true significa recinto encontrado; false hace que el escáner muestre el aviso.
                 */
                QrScannerScreen { id ->
                    if (vm.recinto(id) != null) {
                        nav.navigate(Routes.ficha(id)) {
                            // Al abrir la ficha, se elimina el escáner de la pila.
                            // Así, el botón Volver no reactiva la cámara.
                            popUpTo(Routes.QR_SCANNER) {
                                inclusive = true
                            }
                        }
                        true
                    } else {
                        false
                    }
                }
            }

            composable(Routes.FAVORITOS) {
                CatalogoScreen(vm, navegar, soloFavoritos = true)
            }

            composable(Routes.FICHA) {
                FichaScreen(it.numero("id"), vm, navegar)
            }

            composable(Routes.EVENTOS) {
                EventosScreen(vm)
            }

            composable(Routes.RUTAS) {
                RutasScreen(vm, navegar)
            }

            composable(Routes.RUTA) {
                RutaScreen(it.numero("id"), vm, navegar, volver)
            }

            composable(Routes.EDITAR_RUTA) {
                EditarRutaScreen(
                    it.numero("id"),
                    it.numero("semilla"),
                    vm,
                    volver
                )
            }

            composable(Routes.VISITAS) {
                VisitasScreen(vm, navegar)
            }

            composable(Routes.NUEVA_VISITA) {
                NuevaVisitaScreen(it.numero("id"), vm) {
                    navegar(Routes.VISITAS)
                }
            }

            composable(Routes.GUIA) {
                GuiaScreen(vm, navegar)
            }

            composable(Routes.GESTION) {
                GestionScreen(vm, navegar)
            }

            composable(Routes.EDITAR_RECINTO) {
                EditarRecintoScreen(it.numero("id"), vm, volver)
            }

            composable(Routes.EDITAR_EVENTO) {
                EditarEventoScreen(
                    it.numero("recintoId"),
                    it.numero("eventoId"),
                    vm,
                    volver
                )
            }

            composable(Routes.PERFIL) {
                PerfilScreen(vm, navegar, salir)
            }
        }
    }
}

/**
 * Obtiene un parámetro numérico de una ruta, por ejemplo el id de un recinto.
 * Si no existe o no es válido, retorna -1.
 */
private fun NavBackStackEntry.numero(clave: String): Int {
    return arguments?.getString(clave)?.toIntOrNull() ?: -1
}
