# PLAN – Tutor Virtual ESUCAR (DSY1105 · Evaluación Parcial 2)

> Documento de trabajo. Fuentes usadas: `Ev. Parcial 2_Estudiante_Encargo_DSY1105.pdf` (la llamaremos **EV**),
> `Caso_DSY1105_Tutor Virtual.pdf` (**CASO**), `Guía 7` (**G7**) y las presentaciones 2.1.1 a 2.5.1.
> Todo lo que **no** sale de esos documentos está marcado como **(supuesto)**.

---

## a) El caso en palabras simples

**Quién:** la Escuela de Suboficiales de Carabineros (ESUCAR). Tiene unos 1.200 estudiantes al año (cabos y sargentos)
que deben hacer su **Trabajo de Título I, II y III** para titularse.

**Problema:** hoy los estudiantes avanzan solos con material estático (Word, PDF, PPT) y carpetas en Drive. Se pierden
con las etapas del método, olvidan fechas de entrega y le hacen a los profesores **siempre las mismas preguntas**
(citas APA, formato, estructura, cómo entregar). Los profesores guía terminan saturados de correos y mensajes.

**Qué app hay que hacer:** un **prototipo (MVP)** de app Android que funcione como un "tutor de bolsillo":

1. Muestra la **guía metodológica paso a paso** (Planteamiento, Marco Teórico, Metodología, Recogida de información,
   Análisis de resultados…).
2. Tiene un **buscador de preguntas frecuentes (FAQ)** por categoría (Citación, Metodología, Formato).
3. Permite **seguir los hitos/entregas** con su estado (Por iniciar → En desarrollo → Enviado → Aprobado) y su fecha límite.
4. Muestra las **pautas/rúbricas simplificadas** de cada etapa.
5. **Recuerda las fechas límite** con notificaciones locales y funciona **sin internet**.

**Para quién:** usuario principal = **estudiante**. Usuario supervisor = **profesor guía** (define estructura, aprueba hitos).

---

## b) Checklist de requisitos

### b.1 Rúbrica de la EV (pondera 8% de la asignatura · trabajo en parejas)

| Criterio rúbrica | Pondera | Qué piden (resumido) |
|---|---|---|
| **IE 2.1.1** Interfaz coherente, jerárquica y navegación funcional | **15%** | Diseño ordenado, elementos bien distribuidos, navegación fluida entre vistas, usabilidad |
| **IE 2.1.2** Formularios completos con validación visual por campo e íconos | **15%** | *Todos* los formularios necesarios, cada campo con error visible + ícono. <60% de formularios = 0% |
| **IE 2.2.1** Validación centralizada y desacoplada de la UI | **10%** | Las reglas viven en la lógica (ViewModel/validador), no dentro del Composable |
| **IE 2.2.2** Animaciones funcionales | **10%** | Animaciones que den fluidez y retroalimentación, no de adorno |
| **IE 2.3.1** Proyecto modular + persistencia local | **15%** | Carpetas ordenadas, MVVM, separación de responsabilidades, almacenamiento local funcionando |
| **IE 2.3.2** Colaboración: GitHub + Trello | **20%** | Commits distribuidos entre **ambos** integrantes, issues resueltos, tareas visibles en Trello |
| **IE 2.4.1** Al menos 2 recursos nativos | **15%** | Funcionan, piden permisos de forma segura y se integran en el flujo de la UI |
| **Total** | **100%** | |

### b.2 Requisitos detallados (EV + CASO)

Leyenda: ⬜ pendiente · ✅ hecho · 🟡 parcial

| # | Requisito | Fuente | Criterio | Estado |
|---|---|---|---|---|
| R1 | Interfaz visual organizada y con navegación clara | EV | IE 2.1.1 | 🟡 hay menú con botones, sin Navigation |
| R2 | Diseño claro, **alto contraste**, texto legible, navegación intuitiva | CASO 3.3 | IE 2.1.1 | ⬜ |
| R3 | Funcionar en **teléfonos y tablets** (adaptable) | CASO 3.3 | IE 2.1.1 | ⬜ |
| R4 | Formularios validados con **íconos y mensajes** por campo | EV | IE 2.1.2 | ⬜ |
| R5 | Validaciones manejadas desde la lógica, no acopladas a la vista | EV | IE 2.2.1 | ⬜ |
| R6 | La UI responde a cambios de estado (StateFlow) | EV | IE 2.2.1 | ⬜ |
| R7 | Animaciones que aporten fluidez o retroalimentación | EV | IE 2.2.2 | ⬜ |
| R8 | Estructura modular con **MVVM** | EV, G7 | IE 2.3.1 | 🟡 hay `model` y `repository`, falta `viewmodel` |
| R9 | **Persistencia local** funcionando | EV | IE 2.3.1 | ⬜ |
| R10 | Leer guías/contenidos **sin conexión** | CASO 3.3 | IE 2.3.1 | 🟡 la guía está en código (offline), falta FAQ/pautas |
| R11 | Repositorio GitHub con commits técnicos y distribuidos | EV | IE 2.3.2 | 🟡 repo existe (3 commits, 1 autor) |
| R12 | Planificación en **Trello** (o similar) con tareas repartidas | EV, G7 | IE 2.3.2 | ⬜ (lo hacen ustedes, no es código) |
| R13 | Acceso a **al menos 2 recursos nativos** integrados en la UI | EV | IE 2.4.1 | ⬜ |
| R14 | **Notificaciones locales** para recordar fechas límite | CASO 3.3 | IE 2.4.1 | ⬜ |
| R15 | **Guía metodológica** paso a paso por etapas | CASO 3.1 | IE 2.1.1 | 🟡 lista de 5 etapas con contenido de ejemplo |
| R16 | **Buscador de FAQ** (pregunta, categoría, respuesta) | CASO 3.1 / 3.2 | IE 2.1.1 / 2.2.1 | ⬜ |
| R17 | **Hitos**: título, descripción, **formato requerido**, fecha límite | CASO 3.2 | IE 2.3.1 | 🟡 modelo `Hito` existe pero le falta "formato requerido" |
| R18 | **Estado del entregable**: Por iniciar / En desarrollo / Enviado / Aprobado | CASO 3.2 | IE 2.2.1 | 🟡 `enum EstadoHito` ya existe |
| R19 | **Pautas y rúbricas** simplificadas por etapa | CASO 3.2 | IE 2.1.1 | ⬜ |
| R20 | Alertas / **listas de chequeo** de hitos clave | CASO 4.1 | IE 2.2.2 | ⬜ |
| R21 | Perfil **Profesor guía** (aprueba hitos) | CASO 3.1 | — | ⬜ **(supuesto: modo simple, ver dudas)** |
| R22 | README.md con descripción, **nombres de estudiantes**, funcionalidades y pasos para ejecutar | EV | Entrega | ⬜ |
| R23 | No requiere conexión con servidores de la institución | CASO 3.4 | — | ✅ (no hay backend) |
| R24 | Formulario de **perfil del estudiante** | — | IE 2.1.2 | ⬜ **(supuesto)** |
| R25 | Pantalla de **configuración** (notificaciones, alto contraste) guardada localmente | — | IE 2.3.1 | ⬜ **(supuesto)** |
| R26 | Foto de evidencia del avance / foto de perfil con **cámara y galería** | — | IE 2.4.1 | ⬜ **(supuesto: es la forma propuesta de cumplir R13)** |

---

## c) Entregables

| Qué | Detalle (según EV) |
|---|---|
| Modalidad | **Encargo en parejas**, 8% de la nota. 5 semanas. |
| Fechas | Se asigna en **semana 6**, se entrega en **semana 11**, *antes* de la primera sesión de la Parcial 3 (presentación/defensa). **No hay fecha exacta en el documento** → preguntar. |
| Código | Proyecto Android Studio funcional (carpeta `app`, `build.gradle`, etc.). |
| GitHub | Repositorio **público** con acceso al docente y a ambos integrantes. Commits técnicos, bien escritos y distribuidos. |
| README.md | Descripción, **nombres de los/las estudiantes**, funcionalidades, pasos para ejecutar. |
| Trello | Tablero con distribución de tareas (G7 pide listas "Por hacer / En curso / Finalizado"). |
| AVA grupal | **Un** integrante sube el código fuente (lo mismo que el repo). Si no se sube → **1.0** grupal e individual. |
| AVA individual | Cada uno "activa" el enlace con nombre, apellido, número de equipo y nombre de la app. Si no → sin defensa y 1.0 individual. |
| ⚠️ Congelamiento | Cualquier cambio en el repo **después de la fecha de entrega** y antes de la evaluación = **1.0** automático. |
| Defensa | Es parte de la Parcial 3 (siguiente evaluación). |
| Informe / video / presentación | **No se piden** en la EV. (La G7 pidió un "informe simple" del tema, pero era actividad formativa.) |

---

## d) Requisito → tema de clase → técnica

Solo técnicas que aparecen en las presentaciones o en la G7.

| Requisito | Tema | Técnica |
|---|---|---|
| R8 estructura MVVM | 2.1.1, G7, 2.2.3 (diap. 11-12) | Carpetas `ui/`, `viewmodel/`, `model/`, `repository/`; ViewModel como puente View↔Model |
| R1 pantallas base | 2.1.3 | `@Composable`, `Text`, `Button`, `Image`, `Column`, `Row`, `Card`, `Modifier`, `Scaffold` + `@Preview` |
| R2 alto contraste | 2.2.1 | `MaterialTheme`: `colorScheme` (Theme.kt/Color.kt), `typography` (Type.kt), `shapes` |
| R3 teléfono y tablet | 2.2.1, 2.2.3 | **Window Size Classes** (`calculateWindowSizeClass`): `NavigationBar` en compacto, `NavigationRail` en mediano/expandido |
| R1 navegación | 2.2.3 | `NavController`, `NavHost`, rutas; `TopAppBar`, `NavigationBar` + `NavigationBarItem` |
| R4 formularios | 2.3.1 | `OutlinedTextField`, `Checkbox`, `Switch`, `RadioButton`, `DatePicker`, `Button`/`OutlinedButton`/`TextButton`, `Icon`, `Spacer`, `Divider` |
| R5/R6 validación en lógica | 2.3.1, 2.3.2 | `data class` de UiState + `data class` de errores, `ViewModel` con `StateFlow`, vista con `collectAsState()`; errores en el campo, `AlertDialog` o `Snackbar` |
| Datos entre pantallas | 2.3.2 | ViewModel compartido dentro del `NavHost` |
| R7 animaciones | 2.4.1 | `animateColorAsState`, `animateDpAsState`, `AnimatedVisibility`, loaders (`CircularProgressIndicator`/`LinearProgressIndicator`) |
| Estado en pantalla | 2.4.1 | `remember`, `mutableStateOf`, `rememberSaveable`, `derivedStateOf` (ej.: filtro del buscador) |
| R25 preferencias | 2.4.1 | **DataStore** (preferencias simples: nombre, alto contraste, notificaciones on/off) |
| R9/R17/R18 hitos | 2.5.1 | **Room** sobre SQLite: `@Entity`, `@Dao`, `@Database`, CRUD, consultas con `Flow` |
| R13/R26 cámara y galería | 2.4.3 | Permisos en `AndroidManifest.xml` + permiso en tiempo de ejecución, **ActivityResult API** (`TakePicture`, `GetContent`), mostrar la imagen en la UI, manejar "permiso denegado" amablemente |
| R14 notificaciones | 2.4.3 (pasos generales) | Permiso `POST_NOTIFICATIONS` en tiempo de ejecución + `NotificationManager` del sistema. ⚠️ **No se vio en una diapositiva específica**: se aplican los "pasos generales para integrar cualquier recurso nativo" (diap. 6). Ver dudas. |
| R12 Trello / R11 GitHub | 2.1.1, G7 | Tablero Trello (Por hacer / En curso / Finalizado), repo con colaboradores, ramas y commits |

**Librerías que habría que agregar** (todas vistas en clase): Navigation Compose, `lifecycle-viewmodel-compose`,
Room (+ KSP, que es el compilador de Room), DataStore Preferences, `material3-window-size-class`.
Probablemente también `material-icons-extended` (para los íconos de error/campos) → **lo consulto antes de agregarla**.

**Librería que sobra:** `build.gradle.kts` tiene **Retrofit + Gson** (no se vieron en clase y no se usan) → propongo quitarlas.

---

## e) Arquitectura propuesta (explicada simple)

La idea de MVVM es que **cada parte tenga un solo trabajo**:

- **Model (datos):** qué información existe (un hito, una pregunta FAQ) y dónde se guarda.
- **ViewModel (cerebro):** recibe lo que hace el usuario, valida, decide y guarda el "estado" de la pantalla.
- **View (pantalla):** solo dibuja lo que el ViewModel le dice. No toma decisiones.

```
cl.duoc.tutorvirtual/
├── MainActivity.kt                 ← solo arranca la app y el tema
├── data/
│   ├── model/                      ← clases de datos (EtapaMetodologica, Hito, PreguntaFrecuente, Pauta, Perfil)
│   ├── local/
│   │   ├── room/                   ← HitoEntity, HitoDao, AppDatabase (Room)
│   │   └── datastore/              ← PreferenciasDataStore (perfil, alto contraste, notificaciones)
│   └── repository/                 ← GuiaRepository, FaqRepository, HitoRepository, PerfilRepository
├── validation/                     ← reglas de validación puras (sin Compose): HitoValidator, PerfilValidator
├── viewmodel/                      ← GuiaViewModel, FaqViewModel, HitosViewModel, HitoFormViewModel, PerfilViewModel
├── nativo/                         ← ayudantes de cámara/galería y notificaciones
└── ui/
    ├── navigation/                 ← Rutas + AppNavHost + barra inferior / rail lateral
    ├── components/                 ← piezas reutilizables (CampoValidado, TarjetaHito, ChipEstado…)
    ├── screens/                    ← una pantalla por archivo
    └── theme/                      ← colores alto contraste, tipografía
```

(G7 sugiere `ui/ viewmodel/ model/ repository/`; la diapositiva 2.2.3-12 dice que los nombres pueden variar si se
respeta MVVM. Mantengo `data/model` y `data/repository` porque ya existen en el repo.)

**Pantallas**

| Pantalla | Qué hace |
|---|---|
| Inicio | Saludo con el nombre, progreso general (barra animada), próximo hito por vencer, accesos rápidos |
| Guía metodológica | Lista de etapas → detalle con orientaciones + **pauta/rúbrica** de la etapa (se expande con animación) |
| Preguntas frecuentes | Buscador + filtro por categoría (Citación / Metodología / Formato); la respuesta se despliega animada |
| Mis hitos | Lista de hitos guardados en Room, con color según estado, checklist y progreso |
| Formulario de hito | Crear/editar hito: título, descripción, formato requerido, fecha límite (DatePicker), estado (RadioButton), foto de evidencia |
| Perfil | Formulario validado del estudiante + foto (cámara/galería) + ajustes (Switch notificaciones / alto contraste) |

**Flujo de datos (ejemplo: guardar un hito)**
`Pantalla` → llama `viewModel.onTituloChange()` → el ViewModel usa `HitoValidator` → actualiza `StateFlow` → la pantalla
se redibuja mostrando el error con ícono → al tocar "Guardar" el ViewModel llama a `HitoRepository` → Room lo guarda →
la lista de hitos (que observa un `Flow` de Room) se actualiza sola.

---

## f) Plan de implementación (etapas pequeñas)

Cada etapa termina con `./gradlew assembleDebug` OK + commit + explicación corta.

| Etapa | Qué se hace | Cubre |
|---|---|---|
| 0 | Ordenar: `.gitignore` (excluir PDF/PPTX de `Prueba2/` y archivos personales de `.idea`), quitar Retrofit, agregar librerías vistas en clase, crear carpetas | R8 |
| 1 | Tema: colores de alto contraste, tipografía legible, usar el tema propio en `MainActivity` | R2 |
| 2 | Navegación: rutas, `NavHost`, `TopAppBar`, `NavigationBar` (y `NavigationRail` en tablet), pantallas vacías | R1, R3 |
| 3 | Guía metodológica con `GuiaViewModel` + pautas/rúbricas por etapa | R15, R19, R10 |
| 4 | FAQ: modelo, repositorio, buscador y filtro por categoría en `FaqViewModel` | R16, R10 |
| 5 | Room: `HitoEntity`, `HitoDao`, `AppDatabase`, `HitoRepository`; pantalla "Mis hitos" con estado y progreso | R9, R17, R18, R20 |
| 6 | Formulario de hito con `HitoValidator` + `HitoFormViewModel` (errores con ícono, DatePicker, Snackbar/AlertDialog) | R4, R5, R6 |
| 7 | Perfil + DataStore: formulario validado, ajustes con Switch | R24, R25, R4, R5 |
| 8 | Recurso nativo 1: **cámara + galería** (permisos, ActivityResult, mostrar foto, manejo de permiso denegado) | R13, R26 |
| 9 | Recurso nativo 2: **notificaciones locales** de hitos próximos a vencer (permiso en tiempo de ejecución) | R13, R14 |
| 10 | Animaciones y pulido: colores de estado animados, `AnimatedVisibility`, loader al guardar, barra de progreso animada | R7 |
| 11 | Pruebas unitarias de los validadores (JUnit, ya viene en el proyecto) | R5 (evidencia) |
| 12 | Cierre: actualizar este checklist, `README.md`, `DEFENSA.md` | R22 |

**Importante para el 20% de colaboración (IE 2.3.2):** yo solo puedo hacer commits desde este computador.
La rúbrica pide commits **de ambos integrantes**, issues y Trello. Sugerencia: crear en Trello una tarjeta por etapa,
repartirlas, y que tu pareja haga algunas etapas (o partes) desde su cuenta.

---

## g) Dudas para preguntarle al profesor

1. **Fecha y hora exacta** de entrega (el documento solo dice "semana 11").
2. **¿Repo público o privado?** La EV dice *público*; la G7 decía *privado*.
3. **¿Las notificaciones locales cuentan como recurso nativo?** ¿Y cámara + galería cuentan como **dos** recursos distintos o como uno?
4. Para programar recordatorios en una fecha futura normalmente se usa **WorkManager** o **AlarmManager**, que **no se vieron en clase**. ¿Se pueden usar, o basta con avisar al abrir la app los hitos que vencen pronto?
5. **¿Se puede usar `material-icons-extended`** (más íconos para los formularios)? ¿Y librerías no vistas en clase en general?
6. **Perfil profesor guía:** ¿hay que implementarlo en este avance? ¿Basta un "modo profesor" simple para marcar un hito como *Aprobado*, o puede quedar para la EFT? ¿Se necesita login?
7. **Contenido real:** ¿hay material autorizado de ESUCAR (guía, FAQ, rúbricas) o usamos contenido de ejemplo?
8. El caso dice "actualizar datos cuando haya internet", pero también que **no se necesita conexión a servidores**. ¿Basta con que todo funcione offline?
9. **¿Cuántos/qué formularios** se consideran "necesarios" para el 100% de IE 2.1.2?
10. La EV dice "caso definido por los propios estudiantes". ¿Confirmar que **Tutor Virtual ESUCAR** es nuestro caso oficial?
11. ¿Está permitido usar **asistentes de IA** para programar, y hay que declararlo? (Los commits que haga yo llevarán una línea "Co-Authored-By: Claude".)
