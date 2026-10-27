/*
 * LoginScreen.kt presenta el acceso de demostración y una introducción a la red.
 * LoginScreen conecta con LoginViewModel; LoginContent dibuja campos, botones y mensajes.
 * Los datos del formulario llegan como parámetros y los cambios se envían por funciones.
 * PresentacionScreen muestra tres páginas informativas; los Preview permiten revisar el diseño.
 * Para explicarlo: la pantalla dibuja y comunica eventos, el ViewModel valida las cuentas.
 */
package cl.duoc.redpatrimonio.view

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.redpatrimonio.model.Rol
import cl.duoc.redpatrimonio.ui.theme.*
import cl.duoc.redpatrimonio.viewmodel.LoginViewModel

/*
 * Conecta el estado del ViewModel con el contenido. Al ingresar, usa login(): Boolean;
 * si es válido entrega el Rol al callback y AppNavigation inicia la sesión.
 */
@Composable
fun LoginScreen(onLoginSuccess: (Rol) -> Unit, loginViewModel: LoginViewModel = viewModel()) {
    LoginContent(loginViewModel.correo, loginViewModel.contrasena, loginViewModel.mostrarError,
        loginViewModel::cambiarCorreo, loginViewModel::cambiarContrasena) {
        val valido = loginViewModel.login()
        loginViewModel.mostrarError = !valido
        loginViewModel.rolAutenticado?.let { if (valido) onLoginSuccess(it) }
    }
}

/*
 * Dibuja el formulario a partir de sus parámetros: no consulta el ViewModel directamente.
 * Los callbacks notifican cada cambio. Esto permite previsualizar el formulario con datos de prueba.
 */
@Composable
private fun LoginContent(correo: String, contrasena: String, mostrarError: Boolean,
    onCorreoChange: (String) -> Unit, onContrasenaChange: (String) -> Unit, onLogin: () -> Unit) {
    /*
     * remember conserva valores durante recomposiciones; rememberSaveable también ayuda
     * a recuperarlos al recrear la actividad cuando Android puede restaurar ese estado.
     * Esto no sustituye una base local ni garantiza guardar información tras cualquier cierre.
     */
    var visible by rememberSaveable { mutableStateOf(false) }
    var menu by remember { mutableStateOf(false) }
    var ayuda by rememberSaveable { mutableStateOf(false) }
    var introduccion by rememberSaveable { mutableStateOf(false) }
    if (ayuda) AlertDialog(onDismissRequest = { ayuda = false }, title = { Text("Acceso de demostración") },
        text = { Text("Usa visitante, docente, guia, encargado o admin. La contraseña de todas las cuentas es 123. El registro y la recuperación por correo se integrarán con el servicio de autenticación.") },
        confirmButton = { TextButton(onClick = { ayuda = false }) { Text("Entendido") } })
    if (introduccion) { PresentacionScreen { introduccion = false }; return }
    Surface(color = PatrimonioFondo, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().imePadding().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Column(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(PatrimonioTerracota,Color(0xFF984522))))
                .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PatrimonioEmblem(Modifier.size(60.dp), color = PatrimonioBlanco)
                Text("Red Patrimonial", style = MaterialTheme.typography.headlineSmall, color = PatrimonioBlanco)
                Text("ARZOBISPADO DE SANTIAGO", style = MaterialTheme.typography.labelMedium, color = PatrimonioBlanco.copy(alpha=.85f))
            }
            Column(Modifier.widthIn(max = 480.dp).fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Encabezado("Bienvenido de vuelta", "Inicia sesión para continuar")
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Correo electrónico o cuenta", style = MaterialTheme.typography.labelLarge)
                    OutlinedTextField(value = correo, onValueChange = onCorreoChange, singleLine = true,
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), isError = mostrarError,
                        placeholder = { Text("Ej.: visitante", style = MaterialTheme.typography.bodyMedium) },
                        leadingIcon = { Icono(Simbolo.CORREO, color = PatrimonioSecundario) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = PatrimonioBlanco,
                            focusedContainerColor = PatrimonioBlanco, unfocusedBorderColor = PatrimonioBorde))
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Contraseña", style = MaterialTheme.typography.labelLarge)
                    OutlinedTextField(value = contrasena, onValueChange = onContrasenaChange, singleLine = true,
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), isError = mostrarError,
                        leadingIcon = { Icono(Simbolo.CANDADO, color = PatrimonioSecundario) },
                        trailingIcon = { TextButton(onClick = { visible = !visible }) { Text(if (visible) "Ocultar" else "Ver") } },
                        /*
                         * Oculta los caracteres visualmente. No cifra ni almacena la contraseña.
                         */
                        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { onLogin() }),
                        colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = PatrimonioBlanco,
                            focusedContainerColor = PatrimonioBlanco, unfocusedBorderColor = PatrimonioBorde))
                }
                Surface(color = PatrimonioCrema, shape = RoundedCornerShape(12.dp)) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Modo demostración · Elige tu perfil", style = MaterialTheme.typography.labelMedium)
                        Box {
                            OutlinedButton(onClick = { menu = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) {
                                Text(Rol.values().find { it.cuenta == correo }?.etiqueta ?: "Seleccionar perfil")
                            }
                            /*
                             * El menú solo rellena la cuenta elegida. Aún hay que escribir 123 y pulsar Iniciar sesión.
                             */
                            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                                Rol.values().forEach { rol -> DropdownMenuItem(text = { Text(rol.etiqueta) }, onClick = { onCorreoChange(rol.cuenta); menu = false }) }
                            }
                        }
                        Text("Contraseña de prueba: 123", style = MaterialTheme.typography.bodySmall, color = PatrimonioSecundario)
                    }
                }
                /*
                 * El mensaje se dibuja al cambiar mostrarError; no se crea manualmente una vista aparte.
                 */
                if (mostrarError) Aviso("Correo o contraseña incorrectos", error = true)
                Accion("Iniciar sesión", onClick = onLogin)
                TextButton(onClick = { ayuda = true }, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("¿Olvidaste tu contraseña?") }
                HorizontalDivider(color = PatrimonioBorde)
                TextButton(onClick = { introduccion = true }, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("¿Primera vez aquí? Conoce la red") }
            }
        }
    }
}

/*
 * Presentación local de tres pasos. El índice selecciona textos e ilustración.
 * BackHandler retrocede un paso o regresa al login mediante terminar.
 */
@Composable
private fun PresentacionScreen(terminar: () -> Unit) {
    var pagina by rememberSaveable { mutableStateOf(0) }
    BackHandler { if (pagina > 0) pagina-- else terminar() }
    val titulos = listOf("Conservamos lo que nos conecta", "Explora con propósito", "Súmate a la red")
    val textos = listOf("Descubre, valora y protege el patrimonio de nuestra comunidad para las generaciones futuras.",
        "Recorre lugares, historias y colecciones que dan vida a nuestra identidad.",
        "Planifica una visita y participa del cuidado y la difusión de nuestro patrimonio.")
    Surface(color = PatrimonioFondo) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
            IlustracionRecinto(Modifier.fillMaxWidth().height(260.dp), pagina)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { i -> Box(Modifier.size(8.dp).background(if(i==pagina) PatrimonioTerracota else PatrimonioBorde, RoundedCornerShape(4.dp))) }
            }
            Text(titulos[pagina], style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            Text(textos[pagina], style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center, color = PatrimonioSecundario)
            Accion(if(pagina==2) "Comenzar" else "Siguiente") { if(pagina==2) terminar() else pagina++ }
            TextButton(onClick = terminar) { Text("Omitir") }
        }
    }
}

/*
 * Vista previa de diseño con campos vacíos; no ejecuta un inicio de sesión real.
 */
@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun LoginPreview() { RedPatrimonioReligiosoTheme { LoginContent("", "", false, {}, {}, {}) } }

/*
 * Vista previa de error con tamaño de texto aumentado para revisar legibilidad.
 */
@Preview(showBackground = true, widthDp = 320, fontScale = 1.5f)
@Composable
private fun LoginErrorPreview() { RedPatrimonioReligiosoTheme { LoginContent("admin", "1234", true, {}, {}, {}) } }
