# Red de Patrimonio Religioso

## Estado al 16 de octubre de 2026

Prototipo Android con datos simulados, autorizado por el usuario. El usuario confirmó
que la versión anterior funcionó. Esta entrega rediseña la interfaz con el PDF correcto
y Figma; revisada estáticamente, pendiente compilar y probar en Android Studio.
No afirmar que compila hasta ejecutar las verificaciones indicadas en PRUEBAS_Y_ALCANCE.txt.

## Tecnología y configuración

Kotlin, Jetpack Compose, Material 3, MVVM y Navigation Compose.
Paquete: `cl.duoc.redpatrimonio`. minSdk 36, compileSdk/targetSdk 37.
Gradle, versiones y dependencias originales conservados.
Paleta corregida: terracota #B85C38, dorado #C9A227, crema #F5F1EA, texto #2B2B2B.
Poppins e Inter locales en `res/font`, con licencias en `licenses`.
Referencias y decisiones visuales: `DISENO_MIDFI.txt` en la raíz.

## Organización

- `model/PatrimonioModels.kt`: recintos, eventos, rutas, visitas, roles y estado local.
- `model/DatosDemo.kt`: catálogo ficticio inicial.
- `model/ReglasPatrimonio.kt`: búsquedas, fechas, validaciones y orden de paradas.
- `model/AlmacenDemo.kt`: serialización JSON en SharedPreferences.
- `viewmodel/LoginViewModel.kt`: cuentas simuladas, sin autenticación real.
- `viewmodel/PatrimonioViewModel.kt`: estado observable, permisos de demostración y operaciones.
- `view`: pantallas y componentes Compose; los formularios mantienen borradores con rememberSaveable.
- `navigation`: rutas e integración de pantallas.
- `ui.theme`: tema claro Mid-Fi, colores y tipografías locales.
- `view/IconosPatrimonio.kt`: iconos Canvas, sin dependencia adicional.
- `view/PatrimonioEmblem.kt`: emblema e ilustraciones vectoriales.

## Funciones preparadas

Login, inicio, catálogo, búsqueda y filtros, ficha patrimonial, eventos, favoritos,
rutas personales con orden de paradas, recorrido manual simulado, planificación y
cancelación de visitas, disponibilidad y aceptación/rechazo de guía, confirmación por
administrador, gestión de recintos/eventos y perfiles. Persistencia local por cuenta.
Mapas esquemáticos; los tiempos son estimaciones académicas, sin GPS.

## Interfaz actualizada

Inicio distinto por rol y navegación inferior por perfil. Fichas con pestañas; rutas
en tres pasos; visitas con datos, guía, revisión y confirmación de envío; lista filtrada
con detalle desplegable y disponibilidad del guía en los próximos siete días.
El mapa continúa siendo ilustrativo. Las fotos, registro y recuperación reales no están
integrados. El modelo, ViewModels, almacenamiento, Gradle y tests no se modificaron.

## Cuentas de prueba

`visitante`, `docente`, `guia`, `encargado`, `admin`; contraseña `123` para todas.
El encargado solo administra el recinto 1. El administrador gestiona todos.
Una sesión termina al cerrar sesión o al terminarse el proceso de la app.
Los datos persistidos permanecen en la instalación; no se exportan en el ZIP del código.

## Reglas y límites

- Implementar la tarea solicitada, manteniendo Kotlin, Compose, Material 3 y MVVM.
- No cambiar Gradle ni dependencias sin una solicitud que autorice ese alcance.
- Backend, Room, Retrofit, GPS y JWT quedan para una etapa posterior.
- No utilizar estos perfiles ni datos de demostración como seguridad o información real.
- Conservar el historial: las rutas con visitas asociadas no se editan ni eliminan.
- Las visitas se validan contra fecha/hora futura, cantidad 1–50 y estado ABIERTO de recintos.
- Horarios en texto y disponibilidad del guía por día son simplificaciones del prototipo.
- Explicar cambios y registrar archivos modificados.

## Próximo paso

Abrir el ZIP limpio en el otro computador; sincronizar, compilar, ejecutar pruebas
unitarias/instrumentadas y validar visualmente. Ver LEEME_PRIMERO.txt.
