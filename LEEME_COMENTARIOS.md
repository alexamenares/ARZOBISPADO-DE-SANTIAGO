# Proyecto Android comentado

Esta copia conserva el código funcional del proyecto recibido y añade explicaciones en español para estudiar y preparar la evaluación.

## Qué encontrarás

- Un comentario al principio de cada archivo Kotlin, XML y de configuración relevante que explica para qué sirve y cómo participa en la app.
- Explicaciones de funciones, estados, eventos de interfaz, validaciones, navegación, Room y lectura QR.
- Aclaraciones sobre los datos reales de esta versión: cuentas de demostración, catálogo ficticio, mapa ilustrativo y almacenamiento local.
- El mismo código ejecutable y los mismos valores de configuración que en el ZIP original.

Se comentaron 49 archivos: 31 archivos Kotlin, 3 scripts Gradle Kotlin, 10 XML, el catálogo TOML y 4 archivos de propiedades o reglas.

No se modificaron las bibliotecas del Gradle Wrapper, fuentes, iconos binarios ni documentos originales. Los scripts `gradlew` y `gradlew.bat` se conservan como herramientas generadas, sin reescribirlos.

## Cómo usar esta copia

Para conservar los datos y el entorno de tu proyecto que ya funciona, puedes incorporar únicamente los archivos comentados a una copia de respaldo de ese mismo proyecto:

1. Cierra la ejecución de la aplicación y respalda tu carpeta actual.
2. Descomprime este ZIP en una carpeta diferente.
3. Copia `app/src`, `app/build.gradle.kts`, `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`, `gradle/libs.versions.toml`, `gradle/gradle-daemon-jvm.properties` y `gradle/wrapper/gradle-wrapper.properties` a las mismas ubicaciones de tu copia de trabajo. Los archivos originales correspondientes solo recibieron comentarios.
4. Conserva el `local.properties` de tu computador: contiene la ruta de tu SDK.
5. Abre Android Studio, sincroniza Gradle y ejecuta la aplicación normalmente.

También puedes abrir directamente la carpeta extraída `RedPatrimonioReligioso` que contiene `settings.gradle.kts`. Android Studio deberá identificar o configurar la ubicación del SDK en ese equipo.

El ZIP no incluye cachés ni resultados de compilación (`.gradle`, `.idea`, `.kotlin`, `build`, `app/build`) ni `local.properties`. Son archivos del entorno local que Android Studio vuelve a generar; el ZIP original no fue alterado.

## Por dónde empezar a estudiar

Las rutas Kotlin de esta tabla parten de `app/src/main/java/cl/duoc/redpatrimonio/`.

| Si te preguntan | Archivo o función que puedes abrir |
|---|---|
| Qué le informa la app al sistema Android | `app/src/main/AndroidManifest.xml` |
| Dónde comienza la interfaz | `MainActivity.kt`, función `onCreate` |
| Cómo se inicia sesión | `view/LoginScreen.kt` y `viewmodel/LoginViewModel.kt`, función `login` |
| Qué significan los roles | `model/PatrimonioModels.kt`, enum `Rol` |
| Cómo se eligen las opciones del menú según rol | `navigation/AppNavigation.kt`, variable `botones` |
| Cómo se pasa de una pantalla a otra | `navigation/Routes.kt` y el `NavHost` de `navigation/AppNavigation.kt` |
| Dónde se cambian colores y fuentes | `ui/theme/Color.kt`, `Theme.kt` y `Type.kt` |
| Dónde se cambian campos y botones compartidos | `view/ComponentesPatrimonio.kt`, funciones `Campo` y `Accion` |
| Cómo se filtran recintos | `view/CatalogoScreens.kt` y `model/ReglasPatrimonio.kt`, función `buscar` |
| Cómo se marca un favorito | `view/CatalogoScreens.kt`, `FichaScreen`, y `viewmodel/PatrimonioViewModel.kt`, `alternarFavorito` |
| Dónde está la tabla local | `model/FavoritoEntity.kt` |
| Dónde están las consultas SQL | `dao/FavoritoDao.kt` |
| Dónde se configura Room | `database/AppDatabase.kt` |
| Qué hace el repositorio | `repository/FavorityRepository.kt`, clase `FavoritoRepository` |
| Cómo persisten rutas y visitas de la demo | `model/AlmacenDemo.kt`, funciones `cargar` y `guardar` |
| Cómo se crea una ruta | `view/RutasScreens.kt`, `EditarRutaScreen`, y `viewmodel/PatrimonioViewModel.kt`, `guardarRuta` |
| Qué valida una solicitud de visita | `model/ReglasPatrimonio.kt`, `validarVisita`, y `viewmodel/PatrimonioViewModel.kt`, `crearVisita` |
| Cómo acepta el guía y confirma el administrador | `viewmodel/PatrimonioViewModel.kt`, `responderGuia` y `confirmarVisita` |
| Cómo se lee y valida el QR | `view/QrScannerScreen.kt`, `startScan`, `rawValue` y `patronQrRecinto` |
| Cómo el QR abre una ficha | `navigation/AppNavigation.kt`, destino `Routes.QR_SCANNER` |
| Para qué sirve cada dependencia | `app/build.gradle.kts` y `gradle/libs.versions.toml` |

## Conceptos para explicarlo con palabras simples

- **Manifest:** ficha de registro de la aplicación ante Android. Declara la actividad de entrada y configuración general; no dibuja pantallas.
- **Composable:** función que describe una parte de la interfaz. Compose vuelve a evaluar lo necesario cuando cambia el estado observado.
- **Estado:** valor que la pantalla observa, por ejemplo un texto escrito o un conjunto de favoritos.
- **remember:** conserva un valor entre recomposiciones mientras su parte de la interfaz permanece en la composición.
- **rememberSaveable:** ayuda a restaurar estado de interfaz ante recreación de la actividad; no reemplaza la persistencia de una base de datos.
- **ViewModel:** coordina datos y acciones para las pantallas. Un cambio de configuración puede recrear la actividad sin perder su ViewModel; al terminar el proceso no se conservan automáticamente sus variables.
- **Callback:** función que un componente recibe para informar una acción. Por ejemplo, la pantalla QR entrega un ID a la navegación.
- **Corrutina y suspend:** permiten esperar operaciones que pueden suspenderse sin bloquear el hilo. `launch` inicia una corrutina y `viewModelScope` la vincula a la vida del ViewModel.
- **Flow:** flujo de valores que puede emitir datos nuevos. Aquí Room emite IDs y el ViewModel los observa con `collect`.
- **Entity:** representación de una tabla Room.
- **DAO:** interfaz con las operaciones SQL permitidas sobre esa tabla.
- **Repository:** intermediario que ofrece guardar, quitar y consultar favoritos al ViewModel.
- **Database:** configuración de Room que reúne tablas y ofrece acceso al DAO.
- **KSP:** herramienta de compilación que genera implementaciones a partir de las anotaciones de Room.
- **Ruta de navegación:** dirección interna de una pantalla; `ficha/{id}` es la plantilla y `ficha/1` un destino concreto.
- **JSON:** formato de texto con campos y listas. En esta demo representa los datos generales guardados con SharedPreferences.
- **Google Code Scanner:** abre el escáner mediante Google Play Services y devuelve el contenido. Nuestra aplicación valida el texto e indica a qué ficha navegar.

## Aclaraciones fieles al código recibido

1. **Room guarda favoritos.** El estado general de rutas, visitas, recintos, eventos y disponibilidad se guarda como JSON en SharedPreferences mediante `AlmacenDemo`. Ambos son locales; esta app no consulta el backend.
2. **Hay una cuenta demo por rol.** Las cuentas son `visitante`, `docente`, `guia`, `encargado` y `admin`; la contraseña fija es `123`. El acceso es simulado.
3. **El campo `correoUsuario` de Room contiene el identificador de cuenta demo.** Su nombre no implica que hoy se exija un correo real.
4. **El QR usa Google Code Scanner.** La cámara se administra desde Google Play Services; se conservaron sus imports correctos y el metadato `barcode_ui`.
5. **El mapa y el recorrido son ilustrativos.** No hay GPS ni cálculo de distancia real. La duración se estima con una fórmula académica.
6. **El archivo `FavorityRepository.kt` contiene la clase `FavoritoRepository`.** Se conservaron esos nombres para no alterar tu proyecto.

## Dos detalles detectados que se documentaron sin modificar

- En `PerfilScreen`, el texto de confirmación de restablecimiento dice que borrará favoritos. La función actual `reiniciarDemo` restablece el JSON general y conserva las filas de favoritos en Room. Se añadió un comentario en el punto exacto, sin cambiar el mensaje ni la lógica.
- La prueba instrumentada `favoritosPersistenYSeSeparanPorCuenta` consulta resultados inmediatamente después de operaciones asíncronas de Room. Además, la limpieza de su SharedPreferences de prueba no aísla la base Room. Se documentó que requiere ajustar esperas y aislamiento antes de considerarla una prueba fiable. No se reescribieron tests ni funcionalidad.

Estos dos detalles son observaciones sobre el código original, no cambios funcionales de esta entrega.

## Comprobaciones realizadas

- Se comparó cada archivo retenido con su correspondiente dentro del ZIP original.
- En Kotlin y scripts Gradle se compararon instrucciones y literales, ignorando únicamente comentarios y espacios externos a los literales.
- Se analizaron los 10 XML y se comprobó que conservan sus elementos, atributos y contenido.
- Se analizó el TOML y se comprobó que versiones, bibliotecas y plugins tienen los mismos valores.
- Se compararon las propiedades activas y reglas de configuración.
- Los recursos restantes se conservaron byte por byte.

La comparación detectó cambios de comentarios en 49 archivos y ningún cambio de código o configuración activa. No se ejecutó una nueva compilación Android ni pruebas instrumentadas en este entorno porque no dispone del SDK Android necesario. La prueba final consiste en sincronizar y ejecutar esta copia en tu Android Studio.
