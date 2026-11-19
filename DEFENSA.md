# DEFENSA – Tutor Virtual ESUCAR

Guía para entender y explicar el código con palabras simples. Están todas las partes de la app y al final
hay 10 preguntas probables del profesor con su respuesta.

---

## 1. La idea general: MVVM en 30 segundos

La app está dividida en capas y **cada una tiene un solo trabajo**:

| Capa | Carpeta | Su trabajo | Ejemplo |
|---|---|---|---|
| **Model** (datos) | `data/` | Qué información existe y dónde se guarda | `Hito`, `HitoDao`, `HitoRepository` |
| **ViewModel** (cerebro) | `viewmodel/` | Recibe acciones del usuario, valida, decide y guarda el **estado** | `HitoFormViewModel` |
| **View** (pantalla) | `ui/` | Solo **dibuja** lo que dice el ViewModel | `HitoFormScreen` |

Hay dos carpetas de apoyo:
- `validation/`: las **reglas** de los formularios. Es Kotlin puro y no sabe nada de pantallas.
- `nativo/`: lo que habla con el **sistema Android** (cámara, galería, notificaciones).

**El flujo, por ejemplo al escribir el título de un hito:**

```
Usuario escribe  →  Pantalla llama viewModel.onTituloChange("Ab")
                 →  ViewModel usa HitoValidator.validarTitulo("Ab") → "Debe tener al menos 5 caracteres"
                 →  ViewModel actualiza su StateFlow (nuevo estado con el error)
                 →  La pantalla, que observa con collectAsState(), se redibuja sola y muestra el error en rojo
```

---

## 2. Cada parte del código

### Arranque
- **`MainActivity.kt`**: es la puerta de entrada. Calcula el tamaño de la pantalla (teléfono o tablet), lee el ajuste
  "texto grande" y dibuja la app con nuestro tema. No tiene lógica.
- **`TutorApp.kt`**: clase `Application`, que existe una sola vez mientras la app está abierta. Crea **una sola vez**
  los repositorios (Room y DataStore) y se los entrega a los ViewModels mediante una **fábrica** (`Factory`). Así todas
  las pantallas usan la misma base de datos.

### Datos (`data/`)
- **`model/`**: las "fichas" de información.
  - `Hito`: título, descripción, formato requerido, fecha límite, estado y foto. Calcula `diasRestantes`,
    `estaVencido` y `estaPorVencer`. Incluye la regla `hitosQueRequierenAviso` (vencidos o con 3 días o menos).
  - `EstadoHito`: Por iniciar, En desarrollo, Enviado y Aprobado (los 4 estados del caso).
  - `EtapaMetodologica` + `CriterioPauta`: la guía y su rúbrica simplificada.
  - `PreguntaFrecuente` + `CategoriaFaq`: el banco de dudas.
  - `Perfil`, `Rol`, `Asignatura`, `Ajustes`: datos del usuario.
- **`local/room/`** (clase 2.5.1, **Room = SQLite más fácil**):
  - `HitoEntity`: la **tabla** `hitos`. La fecha se guarda como número (epochDay) y el estado como texto.
  - `HitoDao`: las operaciones **CRUD** (`insertar`, `observarTodos`, `actualizar`, `eliminar`). `observarTodos`
    devuelve un `Flow`, así que **cuando la tabla cambia, la lista se actualiza sola**.
  - `AppDatabase`: crea la base `tutor_virtual.db` (una sola instancia) y, **la primera vez**, inserta 4 hitos de ejemplo.
- **`local/datastore/`** (clase 2.4.1): DataStore guarda datos simples clave-valor, como nombre, rol o los switches.
  Es el reemplazo moderno de SharedPreferences.
- **`repository/`**: la **única puerta** a los datos. Los ViewModels no saben si los datos vienen de Room, de DataStore
  o de una lista fija. Si mañana vienen de internet, solo cambia el repositorio.
  - `GuiaRepository` y `FaqRepository`: contenido incluido en la app, por eso funciona **sin conexión**.
    `FaqRepository.buscar()` quita tildes y mayúsculas para comparar.
  - `HitoRepository`: convierte entre `HitoEntity` (tabla) y `Hito` (modelo).
  - `PreferenciasRepository`: lee y guarda el perfil y los ajustes.

### Validaciones (`validation/`)
- **`HitoValidator`**: título (5 a 60 caracteres), descripción (10 a 300), formato (obligatorio) y fecha (obligatoria).
  Tiene dos **reglas de negocio**:
  1. Un hito **pendiente** no puede tener una fecha **pasada**. Es una dependencia entre los campos fecha y estado.
  2. Solo el **profesor guía** puede marcar un hito como **Aprobado**.
- **`PerfilValidator`**:
  - Nombre: solo letras.
  - RUT: con **dígito verificador módulo 11**.
  - Correo: con formato válido.
  - Asignatura y profesor guía: obligatorios **solo si el rol es estudiante**.
- Ninguna de las dos importa Compose. Por eso se pueden probar con **JUnit** sin emulador (`app/src/test/`).

### ViewModels (`viewmodel/`)
Todos siguen el mismo patrón de la clase 2.3:
1. Un `data class ...UiState` con **todo** lo que la pantalla necesita: valores, errores y si está cargando o guardando.
2. Un `MutableStateFlow` privado y un `StateFlow` público de solo lectura.
3. Funciones `onAlgoChange()` que la pantalla llama al escribir o tocar algo.

| ViewModel | Qué maneja |
|---|---|
| `InicioViewModel` | Junta el perfil (DataStore) y los hitos (Room) con `combine` |
| `GuiaViewModel` | Entrega las etapas |
| `FaqViewModel` | Texto buscado, categoría elegida, resultados y pregunta abierta |
| `HitosViewModel` | Lista de hitos, progreso, marcar como entregado, eliminar y deshacer |
| `HitoFormViewModel` | Formulario crear/editar: valida cada campo al cambiar y todo al guardar |
| `PerfilViewModel` | Formulario de perfil, ajustes y foto |

### Pantallas (`ui/`)
- **`TutorVirtualApp.kt`**:
  - Usa `Scaffold` con `TopAppBar`, que muestra el título y la flecha "volver" en las sub-pantallas.
  - Con **Window Size Class** decide qué barra mostrar: `NavigationBar` abajo en teléfono o `NavigationRail` al
    costado en tablet o pantalla horizontal.
- **`navigation/`**:
  - `Rutas`: los "nombres" de cada pantalla.
  - `AppNavHost`: el **NavHost**, el escenario donde se muestra la pantalla actual, con transiciones animadas.
    Pasa argumentos: `etapaId` y `hitoId`, donde -1 significa "nuevo".
- **`screens/`**: una pantalla por archivo. Solo observan el estado (`collectAsState`) y llaman funciones del ViewModel.
- **`components/`**: piezas reutilizables.
  - `CampoValidado`: campo de texto con borde rojo, ícono de error u "ok", mensaje animado y contador.
  - `TarjetaExpandible`: tarjeta que se abre y cierra, usada en pautas y FAQ.
  - `ChipEstado`: etiqueta de color del estado, con color animado.
  - `ProgresoAnimado`: barra de avance que "crece".
  - `SelectorFoto`: cámara y galería.
  - `RevisarRecordatoriosAlIniciar`: revisa los plazos al abrir la app.
- **`theme/`**:
  - Colores verde, blanco y dorado con **alto contraste**, en modo claro y oscuro.
  - Tipografía más grande, con opción "texto grande".
  - Se desactivó el "color dinámico" para no perder contraste.

### Recursos nativos (`nativo/` + `SelectorFoto`) — clase 2.4.3
Se siguen los pasos de la diapositiva "Pasos generales para integrar cualquier recurso nativo":
1. **Permiso declarado** en `AndroidManifest.xml` (`CAMERA`).
2. **Pedido en tiempo de ejecución** con `RequestPermission`, porque es un permiso "peligroso".
3. **API moderna**: ActivityResult API con `TakePicture` (cámara) y `GetContent` (galería).
4. **Resultado mostrado en la UI**: la foto aparece con un `Crossfade`.
5. **Permiso negado manejado amablemente**: mensaje con ícono y botón "Abrir ajustes". La galería sigue disponible.

Detalles de seguridad:
- Las fotos se guardan en la **carpeta privada** de la app (`filesDir/fotos`), donde otras apps no las pueden leer.
- La cámara recibe un acceso **temporal** a ese archivo mediante **FileProvider**.
- La galería **no necesita permiso**, porque el sistema solo entrega la imagen que el usuario eligió. La copiamos
  porque ese acceso es temporal.

### Notificaciones (extra, no se cuentan como recurso nativo)
- `Notificador` crea un **canal** (obligatorio desde Android 8) y muestra un resumen de los hitos urgentes.
- En Android 13 o superior pide el permiso `POST_NOTIFICATIONS`.
- La revisión se hace **al abrir la app**. No se usó WorkManager ni AlarmManager porque no se vieron en clase.

### Animaciones (clase 2.4.1)

| Animación | Dónde | Para qué |
|---|---|---|
| `animateColorAsState` | `ChipEstado` | El color cambia suave al cambiar el estado del hito |
| `animateDpAsState` | Tarjetas de hitos | Los urgentes se "elevan"; al entregarlos bajan |
| `animateFloatAsState` | Barra de progreso, flecha de tarjetas | La barra crece; la flecha gira 180° |
| `AnimatedVisibility` | Pautas, respuestas FAQ, alertas, campos del perfil por rol, error de permiso | Aparecer y desaparecer sin saltos |
| `AnimatedContent` | Botón Guardar, mensajes de los campos | Texto → loader al guardar; ayuda → error |
| `Crossfade` | Foto | Transición entre "sin foto" y la foto nueva |
| `animateItem` | Listas de FAQ e hitos | Las tarjetas se reacomodan al filtrar o borrar |
| Transiciones `NavHost` | Cambio de pantalla | Deslizamiento y desvanecido |
| Loaders | Listas, formularios | `CircularProgressIndicator` mientras carga o guarda |

---

## 3. Diez preguntas probables del profesor

**1. ¿Por qué usaron MVVM y cómo se nota en el proyecto?**
Para separar la pantalla de la lógica y de los datos. Se nota en las carpetas: `ui/` solo dibuja, `viewmodel/` maneja el
estado y las decisiones, y `data/` guarda y entrega la información. Por ejemplo, `HitoFormScreen` no sabe validar ni
guardar: solo llama `viewModel.guardar()` y muestra lo que el `StateFlow` le dice.

**2. ¿Dónde están las validaciones y cómo demuestran que no están acopladas a la vista?**
En `validation/HitoValidator.kt` y `PerfilValidator.kt`. Son funciones de Kotlin puro que reciben texto y devuelven un
mensaje de error o `null`, y no importan nada de Compose. La prueba es que tienen **pruebas unitarias con JUnit**
(`app/src/test`) que corren sin emulador: si estuvieran pegadas a la pantalla, no se podrían probar así.

**3. ¿Qué es un StateFlow y por qué la pantalla se actualiza sola?**
Es un "contenedor de estado observable": siempre tiene un valor y avisa cuando cambia. El ViewModel cambia el valor
con `_estado.update { ... }` y la pantalla lo observa con `collectAsState()`. Cuando llega un valor nuevo, Compose
**recompone** (vuelve a dibujar) solo lo necesario.

**4. ¿Qué diferencia hay entre Room y DataStore, y por qué usaron los dos?**
- **Room** es una base de datos SQLite con tablas. Sirve para listas de registros con CRUD, como los hitos.
- **DataStore** guarda pares clave-valor simples. Sirve para configuraciones y datos sueltos, como el nombre, el rol
  o "texto grande".

Usamos cada uno para lo que fue diseñado, como se vio en las clases 2.5.1 y 2.4.1.

**5. ¿Cómo funciona el CRUD de hitos?**
`HitoDao` tiene `@Insert` (crear), `@Query` (leer, como `Flow`), `@Update` (actualizar) y `@Delete` (eliminar).
`HitoRepository.guardar()` decide entre crear o actualizar según si el id es 0. La pantalla de hitos observa el
`Flow`, así que al guardar o borrar la lista se actualiza sola. Al borrar hay confirmación (`AlertDialog`) y un
`Snackbar` con "Deshacer", que vuelve a insertar el hito.

**6. ¿Cuáles son los dos recursos nativos y cómo manejan los permisos?**
**Cámara** y **galería**:
- **Cámara:** el permiso `CAMERA` está en el Manifest y se pide en tiempo de ejecución solo cuando el usuario toca
  "Cámara". Si lo niega, mostramos un mensaje y un botón para abrir Ajustes.
- **Galería:** usa `GetContent`, que no necesita permiso porque el usuario elige exactamente qué imagen compartir.

Ambas fotos se guardan en la carpeta privada de la app. Para la cámara usamos `FileProvider`, que le da un acceso
temporal y seguro.

**7. ¿Qué pasa si giro el teléfono mientras lleno un formulario o mientras la cámara está abierta?**
- Los datos del formulario están en el **ViewModel**, que sobrevive a la rotación, así que no se pierden.
- Los estados visuales (pauta abierta, calendario abierto, ruta del archivo de la foto pendiente) usan
  `rememberSaveable`, que también sobrevive.

**8. ¿Cómo hicieron la app adaptable a tablet?**
Con **Window Size Classes** (clase 2.2.1). `MainActivity` calcula el ancho una sola vez:
- Si es **Compact** (teléfono), se usa `NavigationBar` abajo.
- Si es **Medium o Expanded** (tablet u horizontal), se usa `NavigationRail` al costado.

Además, el contenido tiene un ancho máximo (`widthIn(max = 700.dp)`) para que no se estire en pantallas grandes.

**9. ¿Qué validación les parece más interesante y por qué?**
- **El RUT con dígito verificador (módulo 11):** se multiplican los dígitos de derecha a izquierda por 2, 3, 4, 5, 6,
  7 y se repite la serie. Luego se suman y se calcula 11 − (suma % 11); si da 11 el dígito es 0, y si da 10 es K.
- **La dependencia entre campos:** un hito pendiente no puede tener una fecha pasada, pero uno enviado sí. Y solo el
  rol profesor guía puede aprobar.

**10. ¿Cómo funcionan las notificaciones si no usan WorkManager?**
Al abrir la app, `RevisarRecordatoriosAlIniciar` mira si los recordatorios están activos en DataStore. Si falta el
permiso (Android 13 o superior), lo pide. Después lee los hitos de Room y, si hay vencidos o con 3 días o menos,
`Notificador` muestra una notificación resumen. La limitación es que **no avisa si la app no se abre**. Para eso
haría falta WorkManager, que no se vio en clase y queda como mejora para la EFT.

---

## 4. Mejoras propuestas para la Evaluación Final (EFT)

- Reemplazar el contenido de ejemplo por el material oficial de ESUCAR.
- Recordatorios programados aunque la app esté cerrada (WorkManager), si el docente lo autoriza.
- Inicio de sesión real para diferenciar estudiante y profesor guía.
- Sincronización con un servidor cuando haya internet (el caso lo menciona como "funcionamiento mixto").
- Pruebas de interfaz con Compose Testing.
