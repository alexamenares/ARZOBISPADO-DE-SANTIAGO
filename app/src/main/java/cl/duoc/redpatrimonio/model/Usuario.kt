/*
 * Usuario.kt es una data class sencilla con cuenta y contraseña.
 * Es un modelo de apoyo del ejercicio inicial; el login actual usa LoginViewModel y Rol.
 * Esta clase no es una tabla Room ni realiza autenticación por sí misma.
 * Para explicarlo: representa datos, pero no valida ni guarda credenciales.
 */
package cl.duoc.redpatrimonio.model

data class Usuario(
    val correo: String,
    val contrasena: String
)
