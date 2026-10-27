/*
 * LoginViewModel.kt administra los datos y la validación del login de demostración.
 * correo, contrasena y mostrarError son estados que Compose puede observar.
 * login devuelve true si reconoce una cuenta de Rol y la contraseña de prueba 123.
 * LoginScreen usa ese resultado para avisar del error o entregar el rol a AppNavigation.
 * Esto es un acceso simulado para clases: no consulta Oracle ni un servidor.
 */
package cl.duoc.redpatrimonio.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cl.duoc.redpatrimonio.model.Rol

/** Autenticación simulada. Reemplazar por un servicio seguro en la etapa de backend. */
class LoginViewModel : ViewModel() {
    /*
     * mutableStateOf crea un estado observable por Compose. by permite leerlo como variable.
     * private set hace que el correo y la contraseña se cambien mediante sus funciones.
     */
    var correo by mutableStateOf("")
        private set
    var contrasena by mutableStateOf("")
        private set
    var mostrarError by mutableStateOf(false)
    var rolAutenticado: Rol? = null
        private set

    /*
     * Actualiza la cuenta escrita y quita el aviso anterior para permitir otro intento.
     */
    fun cambiarCorreo(valor: String) { correo = valor; mostrarError = false }
    /*
     * Actualiza la contraseña escrita y quita el aviso anterior.
     */
    fun cambiarContrasena(valor: String) { contrasena = valor; mostrarError = false }
    /*
     * Comprueba la contraseña fija 123 y busca la cuenta en Rol.
     * Devuelve Boolean: true permite continuar; false hace que LoginScreen muestre un error.
     */
    fun login(): Boolean {
        rolAutenticado = if (contrasena == "123") Rol.values().find { it.cuenta == correo.trim() } else null
        return rolAutenticado != null
    }
    /*
     * Vacía los campos y el rol del login al entrar o salir de la sesión.
     */
    fun limpiar() { correo = ""; contrasena = ""; mostrarError = false; rolAutenticado = null }
}
