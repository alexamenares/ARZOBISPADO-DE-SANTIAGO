package cl.duoc.redpatrimonio.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import cl.duoc.redpatrimonio.model.Rol

/** Autenticación simulada. Reemplazar por un servicio seguro en la etapa de backend. */
class LoginViewModel : ViewModel() {
    var correo by mutableStateOf("")
        private set
    var contrasena by mutableStateOf("")
        private set
    var mostrarError by mutableStateOf(false)
    var rolAutenticado: Rol? = null
        private set

    fun cambiarCorreo(valor: String) { correo = valor; mostrarError = false }
    fun cambiarContrasena(valor: String) { contrasena = valor; mostrarError = false }
    fun login(): Boolean {
        rolAutenticado = if (contrasena == "123") Rol.values().find { it.cuenta == correo.trim() } else null
        return rolAutenticado != null
    }
    fun limpiar() { correo = ""; contrasena = ""; mostrarError = false; rolAutenticado = null }
}
