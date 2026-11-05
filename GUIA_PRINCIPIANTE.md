# Guía para principiantes – Tutor Virtual ESUCAR

Esta guía explica **todo el proyecto desde cero**, sin dar nada por sabido.
Léela en orden: primero las ideas generales y después archivo por archivo.

> Consejo: ten Android Studio abierto al lado y abre cada archivo a medida que lo leas aquí.

---

## Índice

1. [¿Qué es esta app?](#1-qué-es-esta-app)
2. [Funciona sin internet](#2-funciona-sin-internet)
3. [Conceptos básicos que necesitas](#3-conceptos-básicos-que-necesitas)
4. [Cómo está organizado el proyecto (MVVM)](#4-cómo-está-organizado-el-proyecto-mvvm)
5. [Mapa de carpetas](#5-mapa-de-carpetas)
6. [Qué pasa cuando abres la app (paso a paso)](#6-qué-pasa-cuando-abres-la-app-paso-a-paso)
7. [Explicación archivo por archivo](#7-explicación-archivo-por-archivo)
8. [Ejemplo completo: crear un hito](#8-ejemplo-completo-crear-un-hito)
9. [Dónde se guardan los datos](#9-dónde-se-guardan-los-datos)
10. [Cámara, galería y notificaciones](#10-cámara-galería-y-notificaciones)
11. [Validaciones](#11-validaciones)
12. [Animaciones](#12-animaciones)
13. [Pruebas unitarias](#13-pruebas-unitarias)
14. [Archivos de configuración (Gradle y Manifest)](#14-archivos-de-configuración-gradle-y-manifest)
15. [Cómo ejecutar la app](#15-cómo-ejecutar-la-app)
16. [Cambios típicos que te van a pedir](#16-cambios-típicos-que-te-van-a-pedir)
17. [Glosario rápido](#17-glosario-rápido)
18. [Orden recomendado para estudiar el código](#18-orden-recomendado-para-estudiar-el-código)

---

## 1. ¿Qué es esta app?

Es una app para **celular Android** que ayuda a los estudiantes de **Trabajo de Título I, II y III** de ESUCAR.
Tiene 5 secciones (las 5 pestañas de la barra de abajo):

| Pestaña | Qué hace |
|---|---|
| **Inicio** | Te saluda, te avisa si tienes entregas atrasadas o por vencer, muestra tu avance y la próxima entrega. |
| **Guía** | 6 etapas del trabajo de título, cada una con su "paso a paso" y su pauta de evaluación. |
| **Dudas** | Preguntas frecuentes con un buscador y filtros por categoría. |
| **Hitos** | Tus entregas (hitos): puedes crearlas, editarlas, marcarlas como entregadas y borrarlas. |
| **Perfil** | Tus datos (nombre, RUT, correo...), tu foto y los ajustes de la app. |

Un **hito** es simplemente una entrega con fecha: por ejemplo "Avance 2: Marco teórico, vence el 20-11-2026".

---

## 2. Funciona sin internet

La app **no usa internet para nada**:

- **No** tiene el permiso `INTERNET` en el `AndroidManifest.xml` → Android ni siquiera la deja conectarse.
- La guía y las preguntas frecuentes están **escritas dentro del código** (`GuiaRepository.kt` y `FaqRepository.kt`).
- Los hitos se guardan en una **base de datos dentro del teléfono** (Room).
- El perfil y los ajustes se guardan en un **archivo dentro del teléfono** (DataStore).
- Las notificaciones son **locales**: las genera el mismo teléfono, no vienen de un servidor.
- El respaldo automático en la nube de Google está **desactivado** (`android:allowBackup="false"`).

> Lo único que usa internet es **Android Studio/Gradle** cuando descarga las librerías la primera vez
> que abres el proyecto. Eso es parte de la herramienta de desarrollo, no de la app.

---

## 3. Conceptos básicos que necesitas

### 3.1 Kotlin en 2 minutos

```kotlin
val nombre = "Ana"          // val = valor que NO cambia
var edad = 20               // var = variable que SÍ puede cambiar
val apodo: String? = null   // el "?" significa que puede estar vacío (null)

fun saludar(nombre: String): String {   // función que recibe un texto y devuelve un texto
    return "Hola, $nombre"              // $nombre mete la variable dentro del texto
}

val largo = apodo?.length ?: 0  // "?." = solo si no es null; "?:" = si es null usa 0
```

Otras cosas que vas a ver mucho:

| Código | Qué significa |
|---|---|
| `data class Hito(...)` | Una "cajita" de datos. Kotlin le crea solo `copy()`, `equals()`, `toString()`. |
| `enum class EstadoHito { ... }` | Una lista **cerrada** de opciones (Por iniciar, En desarrollo, Enviado, Aprobado). |
| `object FaqRepository` | Una clase de la que existe **una sola** copia en toda la app. |
| `hito.copy(estado = X)` | Crea un hito igual pero con otro estado (los datos no se modifican, se copian). |
| `lista.filter { ... }` | Se queda solo con los elementos que cumplen la condición. |
| `lista.map { ... }` | Transforma cada elemento en otra cosa. |
| `when { ... }` | Como un `if / else if / else` más ordenado. |
| `it` | Nombre automático del elemento dentro de `{ }` cuando hay uno solo. |
| `suspend fun` | Función que puede tardar (ej: leer la base de datos) sin congelar la pantalla. |

### 3.2 Android

- **Activity**: una "ventana" de la app. Aquí hay **una sola**: `MainActivity`.
- **Application**: un objeto que vive mientras la app esté abierta. Aquí es `TutorApp`.
- **AndroidManifest.xml**: la "ficha" de la app: nombre, ícono, permisos, qué Activity abre primero.
- **Permisos**: algunas cosas (cámara, notificaciones) necesitan que el usuario diga "Permitir".

### 3.3 Jetpack Compose (la interfaz)

En vez de diseñar pantallas en XML, aquí **las pantallas son funciones de Kotlin** marcadas con `@Composable`:

```kotlin
@Composable
fun Saludo(nombre: String) {
    Column {                              // pone cosas una debajo de otra
        Text("Hola, $nombre")             // un texto
        Button(onClick = { /* ... */ }) { // un botón
            Text("Aceptar")
        }
    }
}
```

| Componente | Para qué sirve |
|---|---|
| `Column` / `Row` / `Box` | Ordenar elementos en columna / en fila / uno encima de otro |
| `LazyColumn` | Lista con scroll que solo dibuja lo que se ve (eficiente) |
| `Text`, `Button`, `Icon`, `Card` | Texto, botón, ícono, tarjeta |
| `OutlinedTextField` | Campo para escribir |
| `Scaffold` | "Esqueleto" de pantalla: barra superior, barra inferior, botón flotante, etc. |
| `Modifier` | Ajustes de tamaño, márgenes, colores, clic... `Modifier.padding(16.dp).fillMaxWidth()` |

**Idea clave:** en Compose la pantalla se **vuelve a dibujar sola** cuando cambian los datos que muestra.
No le dices "cambia este texto", sino que cambias el dato y Compose redibuja.

`remember { mutableStateOf(...) }` = una variable que la pantalla recuerda entre redibujos.
`rememberSaveable` = igual, pero sobrevive incluso si giras el teléfono.

### 3.4 ViewModel y StateFlow

- **ViewModel**: clase que guarda los datos y la lógica de **una pantalla**. No se borra al girar el teléfono.
- **StateFlow**: una "caja" con un valor que avisa cada vez que cambia. La pantalla la observa con
  `collectAsState()` / `collectAsStateWithLifecycle()` y se redibuja sola.
- **Flow**: parecido, pero es un "río" de datos que van llegando (ej: Room manda la lista nueva cada vez que cambia).

Patrón que se repite en todos los ViewModel:

```kotlin
private val _estado = MutableStateFlow(MiUiState())   // privado: solo el ViewModel lo cambia
val estado: StateFlow<MiUiState> = _estado.asStateFlow() // público: la pantalla solo lo LEE

fun onNombreChange(nuevo: String) {
    _estado.update { it.copy(nombre = nuevo) }        // crea un estado nuevo con el cambio
}
```

### 3.5 Corrutinas (tareas en segundo plano)

Leer o guardar en la base de datos puede tardar. Para que la pantalla no se congele se usa:

```kotlin
viewModelScope.launch {          // "lanza" una tarea en segundo plano
    repositorio.guardar(hito)    // función suspend
}
```

`viewModelScope` se cancela solo cuando la pantalla se cierra para siempre.

---

## 4. Cómo está organizado el proyecto (MVVM)

El proyecto usa **MVVM** = **M**odel – **V**iew – **V**iew**M**odel. Es separar el código en capas,
cada una con **un solo trabajo**:

```
 ┌────────────────────┐   el usuario toca algo     ┌──────────────────────┐
 │   VIEW (pantalla)  │ ─────────────────────────▶ │      VIEWMODEL       │
 │  ui/screens/*.kt   │                            │  viewmodel/*.kt      │
 │  Solo DIBUJA.      │ ◀───────────────────────── │  Lógica + estado     │
 └────────────────────┘   nuevo estado (StateFlow) └──────────┬───────────┘
                                                              │ pide / guarda datos
                                                   ┌──────────▼───────────┐
                                                   │     REPOSITORIO      │
                                                   │ data/repository/*.kt │
                                                   │ Única puerta a datos │
                                                   └──────────┬───────────┘
                                         ┌────────────────────┼──────────────────┐
                                ┌────────▼───────┐   ┌────────▼───────┐  ┌───────▼────────┐
                                │ Room (SQLite)  │   │   DataStore    │  │ Listas fijas   │
                                │ hitos          │   │ perfil/ajustes │  │ guía y FAQ     │
                                └────────────────┘   └────────────────┘  └────────────────┘
```

Además:
- **`validation/`**: las reglas de los formularios (Kotlin puro, sin Android). El ViewModel las usa.
- **`nativo/`**: código que habla con el teléfono (cámara/galería y notificaciones).

**¿Por qué tanto orden?** Porque si mañana cambias de dónde vienen los datos, solo tocas el repositorio;
y las reglas se pueden probar con JUnit sin abrir el emulador.

---

## 5. Mapa de carpetas

```
TutorVirtualESUCAR/
├── app/
│   ├── build.gradle.kts            ← librerías que usa la app y versión de Android
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml ← permisos, nombre de la app, Activity principal
│       │   ├── res/                ← recursos: íconos (drawable), colores, textos
│       │   └── java/cl/duoc/tutorvirtual/
│       │       ├── MainActivity.kt        ← la app EMPIEZA aquí
│       │       ├── TutorApp.kt            ← crea la base de datos y repositorios una vez
│       │       ├── data/
│       │       │   ├── model/             ← "moldes" de datos: Hito, Perfil, Etapa, Pregunta
│       │       │   ├── local/room/        ← base de datos (tabla hitos)
│       │       │   ├── local/datastore/   ← archivo de preferencias (perfil y ajustes)
│       │       │   └── repository/        ← repositorios
│       │       ├── validation/            ← reglas de los formularios
│       │       ├── viewmodel/             ← un ViewModel por pantalla
│       │       ├── nativo/                ← cámara/galería y notificaciones
│       │       └── ui/
│       │           ├── TutorVirtualApp.kt ← barra superior + barra de navegación
│       │           ├── navigation/        ← rutas y NavHost (cambiar de pantalla)
│       │           ├── screens/           ← las pantallas
│       │           ├── components/        ← piezas reutilizables (campo validado, chip, etc.)
│       │           └── theme/             ← colores y letras
│       └── test/                   ← pruebas unitarias (JUnit)
├── gradle/libs.versions.toml       ← versiones de todas las librerías
├── README.md / PLAN.md / DEFENSA.md
└── GUIA_PRINCIPIANTE.md            ← este archivo
```

---

## 6. Qué pasa cuando abres la app (paso a paso)

1. Android lee el `AndroidManifest.xml` y ve que la app se llama `TutorApp` y la ventana principal es `MainActivity`.
2. Se crea **`TutorApp`**. Tiene un `contenedor` con los repositorios, pero **no se crean todavía**
   (`by lazy` = "créalo la primera vez que alguien lo pida").
3. Se abre **`MainActivity.onCreate()`**:
   - Calcula el tamaño de la pantalla (teléfono o tablet).
   - Lee el ajuste "texto grande" desde DataStore.
   - Aplica el tema (`TutorVirtualESUCARTheme`) y dibuja `TutorVirtualApp`.
4. **`TutorVirtualApp`** dibuja la barra superior, la barra de navegación (abajo en teléfono, a la izquierda en tablet)
   y el `AppNavHost`, que muestra la pantalla actual (al inicio: **Inicio**).
5. Al mismo tiempo, `RevisarRecordatoriosAlIniciar()` revisa si hay hitos por vencer y,
   si los recordatorios están activos, muestra una **notificación**.
6. La primera vez que se pide la base de datos, Room la crea y carga **4 hitos de ejemplo**.
7. `InicioScreen` pide su `InicioViewModel`, que junta el perfil (DataStore) y los hitos (Room)
   y la pantalla se dibuja con esos datos.

---

## 7. Explicación archivo por archivo

### 7.1 Arranque

**`MainActivity.kt`** – La única ventana. Llama a `setContent { ... }`, que es donde empieza Compose.

**`TutorApp.kt`** – Contiene `ContenedorApp`, que crea:
- `hitoRepository` (usa la base de datos Room)
- `preferencias` (usa DataStore)

Así **todos** los ViewModels comparten la misma base de datos. A esto se le llama
"inyección de dependencias manual": en vez de que cada pantalla cree su base de datos, se la entregan hecha.

### 7.2 Modelos (`data/model/`)

Son los "moldes" de la información. No hacen nada, solo guardan datos.

| Archivo | Contiene |
|---|---|
| `Hito.kt` | `EstadoHito` (4 estados) y `Hito` (título, descripción, formato, fecha, estado, foto). Además funciones: `diasRestantes()`, `estaVencido()`, `estaPorVencer()` y `hitosQueRequierenAviso()` (los vencidos o que vencen en ≤ 3 días). |
| `Perfil.kt` | `Rol` (estudiante / profesor guía), `Asignatura` (TT I, II, III), `Perfil` y `Ajustes`. |
| `EtapaMetodologica.kt` | Una etapa de la guía con su paso a paso y su pauta (`CriterioPauta`). |
| `PreguntaFrecuente.kt` | `CategoriaFaq` y `PreguntaFrecuente`. |

### 7.3 Base de datos Room (`data/local/room/`)

Room es una forma fácil de usar **SQLite** (una base de datos con tablas, dentro del teléfono).

| Archivo | Qué es |
|---|---|
| `HitoEntity.kt` | La **tabla** `hitos`. Cada propiedad es una columna. La fecha se guarda como número (`epochDay` = días desde 1970) y el estado como texto (`"ENVIADO"`). También tiene `aModelo()` y `aEntidad()` para convertir entre la tabla y el `Hito` que usa la app. |
| `HitoDao.kt` | Las **operaciones** sobre la tabla: leer todos, leer uno, insertar, actualizar, eliminar (CRUD). Room escribe el SQL real por ti al compilar. |
| `AppDatabase.kt` | La **base de datos** completa. `obtener()` la crea solo la primera vez. `DatosIniciales` mete 4 hitos de ejemplo cuando la base se crea. |

Dato importante: `observarTodos()` devuelve un **Flow**. Eso significa que **cada vez que cambia la tabla,
la lista se manda de nuevo** y las pantallas se actualizan solas.

### 7.4 DataStore (`data/local/datastore/PreferenciasDataStore.kt`)

DataStore guarda datos simples tipo **"clave → valor"** (como un diccionario) en un archivo.
`Claves` tiene el nombre de cada dato: `"nombre"`, `"rut"`, `"recordatorios"`, etc.

### 7.5 Repositorios (`data/repository/`)

| Archivo | Qué hace |
|---|---|
| `HitoRepository.kt` | Habla con el DAO. `guardar()` inserta si el hito es nuevo (`id == 0`) o actualiza si ya existe. `restaurar()` sirve para el botón "Deshacer". |
| `PreferenciasRepository.kt` | Lee y guarda el perfil y los ajustes en DataStore. Expone `perfil`, `ajustes` y `esProfesor` como Flow. |
| `GuiaRepository.kt` | La lista fija de las 6 etapas de la guía. **Aquí cambias el contenido de la guía.** |
| `FaqRepository.kt` | La lista fija de preguntas frecuentes y el buscador. `normalizar()` pasa a minúsculas y quita tildes, así "citacion" encuentra "Citación". **Aquí cambias las preguntas.** |

### 7.6 Validaciones (`validation/`)

| Archivo | Reglas |
|---|---|
| `HitoValidator.kt` | Título (5–60 caracteres), descripción (10–300), formato (mín. 3), fecha (obligatoria; si el hito está pendiente no puede ser pasada), estado (solo el profesor guía puede poner "Aprobado"). |
| `PerfilValidator.kt` | Nombre (solo letras, 3–60), RUT (formato y dígito verificador con módulo 11), correo, asignatura y profesor guía (solo obligatorios si eres estudiante). |

Cada función devuelve **un texto con el error** o **`null` si está bien**. `HitoErrores` / `PerfilErrores`
juntan el error de cada campo y `hayErrores` dice si hay al menos uno.

### 7.7 ViewModels (`viewmodel/`)

| Archivo | Pantalla | Qué hace |
|---|---|---|
| `InicioViewModel.kt` | Inicio | Junta (`combine`) el perfil + los hitos y calcula las alertas. |
| `GuiaViewModel.kt` | Guía | Entrega las etapas. |
| `FaqViewModel.kt` | Dudas | Guarda el texto buscado, la categoría elegida y qué pregunta está abierta; vuelve a buscar cada vez que algo cambia. |
| `HitosViewModel.kt` | Mis hitos | Observa la lista de Room, calcula el progreso, marca como entregado, elimina y deshace. |
| `HitoFormViewModel.kt` | Formulario de hito | Carga el hito (si es edición), valida cada campo apenas cambia y guarda. |
| `PerfilViewModel.kt` | Perfil | Formulario del perfil, foto y los dos ajustes. |

**¿Qué es la `Factory`?** Un ViewModel normal no puede recibir parámetros. Como estos necesitan
el repositorio, la `Factory` le dice a Android **cómo crearlos**: "saca el repositorio de `TutorApp` y pásaselo".

`stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), valorInicial)` convierte un Flow en StateFlow:
"mientras alguien esté mirando, mantén el último valor; si nadie mira por 5 segundos, descansa".

### 7.8 Navegación (`ui/navigation/`)

| Archivo | Qué hace |
|---|---|
| `Rutas.kt` | Las "direcciones" de cada pantalla (`"inicio"`, `"guia/{etapaId}"`, `"hito_form?hitoId={hitoId}"`...), las 5 pestañas (`DestinoPrincipal`) y el título de cada pantalla. |
| `AppNavHost.kt` | El `NavHost` es el "escenario": según la ruta actual muestra una pantalla u otra. También define las animaciones al cambiar de pantalla. `navegarPrincipal()` evita que se apilen pantallas repetidas al tocar las pestañas. |

Ejemplo: tocar un hito hace `navController.navigate("hito_form?hitoId=3")` y se abre el formulario
con el hito 3. Sin id (`"hito_form"`) se abre un formulario vacío para crear uno nuevo.

### 7.9 Pantallas (`ui/screens/`)

| Archivo | Qué dibuja |
|---|---|
| `InicioScreen.kt` | Saludo, tarjeta "Completa tu perfil", tarjeta roja de alertas, barra de avance, próxima entrega y accesos rápidos. |
| `GuiaScreen.kt` | `GuiaScreen` (lista de etapas) y `GuiaDetalleScreen` (paso a paso + pauta desplegable + botones Anterior/Siguiente). |
| `FaqScreen.kt` | Buscador, chips de categoría, cantidad de resultados y tarjetas que se abren con la respuesta. |
| `HitosScreen.kt` | Barra de avance, lista de hitos (checkbox, chip de color, fecha, días restantes, botón eliminar), botón "Nuevo hito", diálogo de confirmación y Snackbar "Deshacer". |
| `HitoFormScreen.kt` | Formulario: campos de texto, fecha con calendario, estado con RadioButton, foto y botón guardar. |
| `PerfilScreen.kt` | Foto de perfil, rol, datos personales, asignatura, botón guardar y ajustes con Switch. |

### 7.10 Componentes reutilizables (`ui/components/`)

| Archivo | Qué es |
|---|---|
| `CampoValidado.kt` | Campo de texto que se pone **rojo con ícono de error** si hay error, o con **ícono verde** si está bien. Muestra el mensaje debajo y un contador de caracteres. `MensajeError` muestra un error suelto. |
| `ChipEstado.kt` | Etiqueta de color con el estado del hito (el color cambia con animación). |
| `ProgresoAnimado.kt` | Barra de avance "X de Y hitos entregados" que se llena con animación. |
| `TarjetaExpandible.kt` | Tarjeta que se abre y se cierra al tocarla (la flecha gira). |
| `SelectorFoto.kt` | Botones **Cámara** y **Galería**, pide permiso, muestra la foto y el botón "Quitar foto". |
| `RevisarRecordatorios.kt` | No dibuja nada: al abrir la app revisa los hitos y lanza la notificación. |

### 7.11 Tema (`ui/theme/`)

- `Color.kt`: los colores (verde institucional, dorado, rojo de error y los 4 colores de estado).
- `Theme.kt`: arma el tema claro y el oscuro con esos colores.
- `Type.kt`: tamaños de letra; si "texto grande" está activo, todo se multiplica por 1.2.

### 7.12 Nativo (`nativo/`)

- `GestorFotos.kt`: crea archivos de foto, los copia desde la galería, los borra y los carga en tamaño reducido.
- `Notificador.kt`: crea el canal de notificaciones y muestra el aviso "Tienes entregas próximas".

---

## 8. Ejemplo completo: crear un hito

Sigamos el viaje de un dato desde que tocas el botón hasta que queda guardado:

1. En **Mis hitos** tocas **"Nuevo hito"** → `HitosScreen` llama a `nuevoHito()`.
2. `AppNavHost` navega a `"hito_form"` → se abre `HitoFormScreen` con `hitoId = null`.
3. Se crea `HitoFormViewModel` con un formulario vacío.
4. Escribes "Avance" en el título:
   - El `CampoValidado` llama a `viewModel.onTituloChange("Avance")`.
   - El ViewModel llama a `HitoValidator.validarTitulo("Avance")` → devuelve `null` (está bien).
   - Actualiza `_estado` con el nuevo título y sin error.
   - La pantalla se **redibuja sola** y el campo muestra el ícono verde.
5. Si escribes "Ava" → el validador devuelve `"Debe tener al menos 5 caracteres"` → el campo se pone rojo.
6. Tocas **"Crear hito"** → `viewModel.guardar()`:
   - Valida **todo** con `HitoValidator.validarTodo(...)`.
   - Si hay errores → muestra "Revisa los campos marcados en rojo" en un Snackbar y se detiene.
   - Si está todo bien → `repositorio.guardar(hito)` → `dao.insertar(...)` → se guarda en SQLite.
   - Pone `guardado = true`.
7. `HitoFormScreen` ve `guardado = true` y vuelve atrás (`alTerminar()`).
8. Como `HitosViewModel` está **observando** la tabla con un Flow, recibe la lista nueva
   y el hito aparece en la lista **sin hacer nada más**.

---

## 9. Dónde se guardan los datos

| Qué | Dónde | Archivo en el teléfono |
|---|---|---|
| Hitos | Room (SQLite) | `tutor_virtual.db` |
| Perfil y ajustes | DataStore | `preferencias.preferences_pb` |
| Fotos | Carpeta privada de la app | `files/fotos/foto_XXXX.jpg` |
| Guía y preguntas | Dentro del código | `GuiaRepository.kt`, `FaqRepository.kt` |

Todo queda **solo en el teléfono**. Si desinstalas la app, se borra todo.
Para "empezar de cero" en el emulador: *Ajustes → Apps → Tutor Virtual → Almacenamiento → Borrar datos*.

---

## 10. Cámara, galería y notificaciones

### Cámara (`SelectorFoto.kt` + `GestorFotos.kt`)

1. El permiso `CAMERA` está declarado en el Manifest.
2. Al tocar "Cámara" se revisa si ya hay permiso. Si no, se le pregunta al usuario.
3. Se crea un archivo vacío en la carpeta privada de la app.
4. Como la cámara es **otra app**, no puede escribir en nuestra carpeta privada:
   `FileProvider` le presta una dirección temporal y segura (`content://...`).
5. Se abre la cámara con `ActivityResultContracts.TakePicture()`. Si la foto se toma bien, se guarda la ruta.
6. Si el usuario **niega** el permiso, aparece un mensaje y un botón "Abrir ajustes".

### Galería

`ActivityResultContracts.GetContent()` abre el selector de imágenes del sistema. **No necesita permiso**.
La imagen elegida se **copia** a la carpeta privada, porque el acceso a la original es temporal.

### Notificaciones (`Notificador.kt` + `RevisarRecordatorios.kt`)

- Al abrir la app, si el ajuste "Recordatorios" está activo, se revisan los hitos.
- Desde Android 13 hay que pedir el permiso `POST_NOTIFICATIONS`.
- Si hay hitos vencidos o que vencen en ≤ 3 días, se muestra **una** notificación resumen.
- Al tocarla, se abre la app.
- **Ojo:** solo revisa al **abrir** la app (no hay un proceso en segundo plano).

---

## 11. Validaciones

Hay **4 tipos** de validación:

| Tipo | Ejemplo |
|---|---|
| Campo obligatorio | "El título es obligatorio" |
| Largo mínimo/máximo | "Máximo 60 caracteres" |
| Formato | RUT `12345678-5`, correo `nombre@dominio.cl` |
| Dependencia entre campos | Un hito pendiente no puede tener fecha pasada; el profesor guía no necesita asignatura. |

**Cómo funciona el RUT (módulo 11):** se toman los dígitos de derecha a izquierda, se multiplican por
2, 3, 4, 5, 6, 7, 2, 3..., se suman, y `11 - (suma % 11)` da el dígito verificador (11 → 0, 10 → K).

---

## 12. Animaciones

| Dónde | Animación | Función de Compose |
|---|---|---|
| Cambiar de pantalla | Deslizar + aparecer | `slideInHorizontally`, `fadeIn` en `AppNavHost` |
| Chip de estado | El color cambia suave | `animateColorAsState` |
| Barra de progreso | Se llena poco a poco | `animateFloatAsState` |
| Tarjeta expandible | Se abre/cierra y la flecha gira | `AnimatedVisibility` + `animateFloatAsState` |
| Hitos urgentes | Se "elevan" (más sombra) | `animateDpAsState` |
| Mensaje de error | Cambia suave | `AnimatedContent` |
| Botón guardar | Texto → ruedita de carga | `AnimatedContent` |
| Foto | Transición suave | `Crossfade` |
| Lista de FAQ/hitos | Se reacomoda al filtrar | `Modifier.animateItem()` |

---

## 13. Pruebas unitarias

Están en `app/src/test/`. Prueban la lógica **sin abrir el emulador**:

| Archivo | Qué prueba |
|---|---|
| `HitoValidatorTest.kt` | Reglas del formulario de hito. |
| `PerfilValidatorTest.kt` | Nombre, RUT, correo, dependencias por rol. |
| `ReglasNegocioTest.kt` | Días restantes, hitos vencidos, alertas y buscador de FAQ. |

Una prueba se ve así:

```kotlin
@Test
fun `titulo vacio da error`() {
    assertEquals("El título es obligatorio", HitoValidator.validarTitulo(""))
}
```

Ejecutarlas: clic derecho en la carpeta `test` → **Run Tests**, o en la terminal `./gradlew testDebugUnitTest`.

---

## 14. Archivos de configuración (Gradle y Manifest)

- **`settings.gradle.kts`**: nombre del proyecto y de dónde bajar librerías.
- **`build.gradle.kts`** (raíz): declara los plugins.
- **`app/build.gradle.kts`**: versión mínima de Android (`minSdk = 26` = Android 8), versión objetivo, y la lista
  de librerías (`dependencies`): Compose, Navigation, ViewModel, Room, DataStore, JUnit.
- **`gradle/libs.versions.toml`**: las **versiones** de cada librería en un solo lugar.
- **`AndroidManifest.xml`**: permisos `CAMERA` y `POST_NOTIFICATIONS`, la clase `TutorApp`,
  la `MainActivity` y el `FileProvider` (para la cámara).
- **`res/xml/file_paths.xml`**: qué carpeta puede compartir el `FileProvider` (la de fotos).
- **KSP**: herramienta que, al compilar, genera el código de Room a partir de tus `@Entity`, `@Dao` y `@Database`.

---

## 15. Cómo ejecutar la app

1. Abre la carpeta del proyecto en **Android Studio** y espera que termine "Gradle sync".
2. Crea un emulador en **Device Manager** (Android 8 o superior) o conecta tu celular con depuración USB.
3. Presiona **Run ▶**.

La primera vez verás 4 hitos de ejemplo y te pedirá permiso de notificaciones.

---

## 16. Cambios típicos que te van a pedir

| Quiero... | Dónde |
|---|---|
| Cambiar el texto de la guía | `data/repository/GuiaRepository.kt` |
| Agregar una pregunta frecuente | `data/repository/FaqRepository.kt` (copia una línea `PreguntaFrecuente(...)` y cambia el id) |
| Cambiar los hitos de ejemplo | `data/local/room/AppDatabase.kt` → `DatosIniciales` (solo afecta instalaciones nuevas) |
| Cambiar una regla (ej: título de 3 caracteres) | `validation/HitoValidator.kt` → `TITULO_MIN` |
| Cambiar a cuántos días se avisa | `data/model/Hito.kt` → `estaPorVencer(dias = 3)` |
| Cambiar colores | `ui/theme/Color.kt` |
| Cambiar el título de una pantalla | `ui/navigation/Rutas.kt` → `tituloDeRuta()` |
| Agregar una pantalla nueva | 1) crear `XxxScreen.kt`, 2) agregar ruta en `Rutas.kt`, 3) agregar `composable(...)` en `AppNavHost.kt` |
| Agregar un campo a Hito | `Hito.kt` + `HitoEntity.kt` (y sus conversiones) + subir `version` en `AppDatabase` y borrar datos de la app |

---

## 17. Glosario rápido

| Palabra | Significado simple |
|---|---|
| **Composable** | Función que dibuja una parte de la pantalla. |
| **Estado (state)** | Los datos que la pantalla muestra en este momento. |
| **UiState** | Una `data class` con **todo** lo que una pantalla necesita mostrar. |
| **Recomposición** | Cuando Compose vuelve a dibujar porque cambió el estado. |
| **State hoisting** | El componente no guarda su estado; se lo pasan desde afuera (ej: `TarjetaExpandible` recibe `expandida`). |
| **Flow / StateFlow** | Datos que avisan cuando cambian. |
| **Corrutina** | Tarea que corre en segundo plano sin congelar la pantalla. |
| **Repositorio** | Clase intermedia entre el ViewModel y los datos. |
| **DAO** | Interfaz con las operaciones de la base de datos. |
| **Entity** | Clase que representa una tabla. |
| **CRUD** | Create, Read, Update, Delete (crear, leer, actualizar, borrar). |
| **Singleton** | Algo de lo que existe una sola copia (ej: la base de datos). |
| **Snackbar** | Mensajito temporal abajo de la pantalla. |
| **FAB** | Botón flotante (el "Nuevo hito"). |
| **Permiso en tiempo de ejecución** | Permiso que se pregunta al usuario mientras usa la app. |
| **dp / sp** | Unidades de tamaño: `dp` para espacios, `sp` para letras (respeta el tamaño de letra del usuario). |

---

## 18. Orden recomendado para estudiar el código

1. `data/model/Hito.kt` y `Perfil.kt` → entiende los datos.
2. `validation/HitoValidator.kt` → reglas simples, Kotlin puro.
3. `data/repository/FaqRepository.kt` → una lista y un filtro.
4. `viewmodel/FaqViewModel.kt` + `ui/screens/FaqScreen.kt` → el ejemplo **más simple** de MVVM completo.
5. `ui/navigation/Rutas.kt` y `AppNavHost.kt` → cómo se cambia de pantalla.
6. `data/local/room/*` + `HitoRepository.kt` → la base de datos.
7. `viewmodel/HitosViewModel.kt` + `HitosScreen.kt` → lista que se actualiza sola.
8. `viewmodel/HitoFormViewModel.kt` + `HitoFormScreen.kt` → formulario completo.
9. `PreferenciasRepository.kt` + `PerfilViewModel.kt` + `PerfilScreen.kt` → DataStore.
10. `SelectorFoto.kt`, `GestorFotos.kt`, `Notificador.kt` → lo más avanzado; déjalo para el final.

---

### Cambios hechos al código para simplificarlo

- **`AndroidManifest.xml`**: `allowBackup="false"` (sin respaldo en la nube) y se quitaron las reglas de respaldo
  (`backup_rules.xml` y `data_extraction_rules.xml`), que ya no se usan.
- **`AppDatabase.kt`**: el "singleton" ahora es un `if` simple y los hitos de ejemplo se agregan con una
  función `agregarHito(...)` en vez de una lista de `Triple`.
- **`HitoEntity.kt`** y **`PreferenciasRepository.kt`**: para convertir texto a enum se usa
  `entries.find { it.name == texto }` en vez de `runCatching { valueOf(...) }`.
- **`HitoFormViewModel.kt`**: `onEstadoChange` usa un `if / else` en vez de `?.let { } ?:`.
- **`GestorFotos.kt`**: `try / catch` normal en vez de `runCatching { }.getOrNull()`.

La app hace exactamente lo mismo que antes; solo cambió cómo está escrito.
