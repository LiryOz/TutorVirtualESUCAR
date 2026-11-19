# PLAN – Tutor Virtual ESUCAR (DSY1105 · Evaluación Parcial 2)

> Documento de trabajo. Fuentes usadas: `Ev. Parcial 2_Estudiante_Encargo_DSY1105.pdf` (la llamaremos **EV**),
> `Caso_DSY1105_Tutor Virtual.pdf` (**CASO**), `Guía 7` (**G7**) y las presentaciones 2.1.1 a 2.5.1.
> Todo lo que **no** sale de esos documentos está marcado como **(supuesto)**.
>
> **Estado:** etapas 0 a 12 terminadas. Última actualización: 05-11-2026.

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

| Criterio rúbrica | Pondera | Qué piden (resumido) | Estado |
|---|---|---|---|
| **IE 2.1.1** Interfaz coherente, jerárquica y navegación funcional | **15%** | Diseño ordenado, elementos bien distribuidos, navegación fluida, usabilidad | ✅ |
| **IE 2.1.2** Formularios completos con validación visual por campo e íconos | **15%** | *Todos* los formularios necesarios, cada campo con error visible + ícono | ✅ |
| **IE 2.2.1** Validación centralizada y desacoplada de la UI | **10%** | Reglas en la lógica (ViewModel/validador), no en el Composable | ✅ |
| **IE 2.2.2** Animaciones funcionales | **10%** | Animaciones que den fluidez y retroalimentación | ✅ |
| **IE 2.3.1** Proyecto modular + persistencia local | **15%** | Carpetas, MVVM, separación de responsabilidades, almacenamiento local | ✅ |
| **IE 2.3.2** Colaboración: GitHub + Trello | **20%** | Commits de **ambos** integrantes, issues, tareas visibles en Trello | ⬜ **depende del equipo** (ver nota) |
| **IE 2.4.1** Al menos 2 recursos nativos | **15%** | Funcionan, piden permisos de forma segura, integrados en la UI | ✅ cámara + galería |
| **Total** | **100%** | | |

> ⚠️ **IE 2.3.2 (20%)**: hoy los commits los hizo un solo integrante. Falta que **la pareja también haga commits**
> desde su cuenta, crear **issues** en GitHub y el tablero de **Trello**. Esto no se puede resolver con código.

### b.2 Requisitos detallados (EV + CASO)

Leyenda: ⬜ pendiente · ✅ hecho · 🟡 parcial

| # | Requisito | Fuente | Criterio | Estado | Dónde está |
|---|---|---|---|---|---|
| R1 | Interfaz visual organizada y con navegación clara | EV | IE 2.1.1 | ✅ | `ui/navigation/`, `ui/TutorVirtualApp.kt` |
| R2 | Diseño claro, **alto contraste**, texto legible | CASO 3.3 | IE 2.1.1 | ✅ | `ui/theme/` + ajuste "Texto grande" |
| R3 | Funcionar en **teléfonos y tablets** | CASO 3.3 | IE 2.1.1 | ✅ | WindowSizeClass: barra inferior / NavigationRail |
| R4 | Formularios validados con **íconos y mensajes** por campo | EV | IE 2.1.2 | ✅ | `CampoValidado.kt`, formularios de Hito y Perfil |
| R5 | Validaciones manejadas desde la lógica | EV | IE 2.2.1 | ✅ | `validation/HitoValidator.kt`, `PerfilValidator.kt` |
| R6 | La UI responde a cambios de estado (StateFlow) | EV | IE 2.2.1 | ✅ | todos los `viewmodel/` |
| R7 | Animaciones que aporten fluidez o retroalimentación | EV | IE 2.2.2 | ✅ | ver lista en `DEFENSA.md` |
| R8 | Estructura modular con **MVVM** | EV, G7 | IE 2.3.1 | ✅ | `data/`, `viewmodel/`, `ui/`, `validation/`, `nativo/` |
| R9 | **Persistencia local** funcionando | EV | IE 2.3.1 | ✅ | Room (hitos) + DataStore (perfil y ajustes) |
| R10 | Leer guías/contenidos **sin conexión** | CASO 3.3 | IE 2.3.1 | ✅ | contenido incluido en la app |
| R11 | Repositorio GitHub con commits técnicos y distribuidos | EV | IE 2.3.2 | 🟡 | commits técnicos ✅ · distribución entre ambos ⬜ |
| R12 | Planificación en **Trello** con tareas repartidas | EV, G7 | IE 2.3.2 | ⬜ | lo hace el equipo |
| R13 | Acceso a **al menos 2 recursos nativos** | EV | IE 2.4.1 | ✅ | **cámara** y **galería** (`SelectorFoto.kt`) |
| R14 | **Notificaciones locales** de fechas límite | CASO 3.3 | — (extra, no se cuenta como recurso nativo) | ✅ | `nativo/Notificador.kt` |
| R15 | **Guía metodológica** paso a paso | CASO 3.1 | IE 2.1.1 | ✅ | 6 etapas (`GuiaRepository`) |
| R16 | **Buscador de FAQ** (pregunta, categoría, respuesta) | CASO 3.1/3.2 | IE 2.1.1 | ✅ | 14 preguntas, 4 categorías |
| R17 | **Hitos**: título, descripción, formato requerido, fecha límite | CASO 3.2 | IE 2.3.1 | ✅ | `Hito`, `HitoEntity` |
| R18 | **Estado del entregable** (4 estados) | CASO 3.2 | IE 2.2.1 | ✅ | `EstadoHito` + `ChipEstado` |
| R19 | **Pautas y rúbricas** por etapa | CASO 3.2 | IE 2.1.1 | ✅ | `CriterioPauta`, tarjeta expandible |
| R20 | Alertas / **listas de chequeo** de hitos | CASO 4.1 | IE 2.2.2 | ✅ | checkbox en Hitos + tarjeta de alertas en Inicio |
| R21 | Perfil **Profesor guía** (aprueba hitos) | CASO 3.1 | — | ✅ **(supuesto: rol simple en Perfil, sin login)** | solo ese rol puede marcar "Aprobado" |
| R22 | README.md con descripción, **nombres**, funcionalidades y pasos | EV | Entrega | 🟡 | falta escribir los **nombres** del equipo |
| R23 | No requiere servidores | CASO 3.4 | — | ✅ | sin internet ni backend |
| R24 | Formulario de **perfil** del estudiante | — | IE 2.1.2 | ✅ **(supuesto)** | nombre, RUT, correo, rol, asignatura, profesor guía |
| R25 | **Ajustes** guardados localmente | — | IE 2.3.1 | ✅ **(supuesto)** | recordatorios + texto grande (DataStore) |
| R26 | Foto de evidencia / perfil con **cámara y galería** | — | IE 2.4.1 | ✅ **(supuesto)** | Perfil y formulario de Hito |
| R27 | Pruebas unitarias de la lógica | — | IE 2.2.1 (evidencia) | ✅ **(supuesto)** | 25 pruebas JUnit, todas pasan |

---

## c) Entregables

| Qué | Detalle (según EV) | Estado |
|---|---|---|
| Modalidad | **Encargo en parejas**, 8% de la nota. 5 semanas. | — |
| Fechas | Se asigna en **semana 6**, se entrega en **semana 11**, antes de la Parcial 3. **Fecha exacta: preguntar.** | ⬜ |
| Código | Proyecto Android Studio funcional. | ✅ |
| GitHub | Repositorio **público** (confirmado) con acceso al docente y ambos integrantes. | ⬜ revisar visibilidad y accesos |
| README.md | Descripción, **nombres**, funcionalidades, pasos para ejecutar. | 🟡 falta poner nombres |
| Trello | Tablero con distribución de tareas (Por hacer / En curso / Finalizado). | ⬜ |
| AVA grupal | **Un** integrante sube el código fuente. Si no → **1.0**. | ⬜ |
| AVA individual | Cada uno activa el enlace con nombre, equipo y nombre de la app. Si no → sin defensa. | ⬜ |
| ⚠️ Congelamiento | Cambios en el repo **después de la entrega** = **1.0** automático. | — |
| Informe / video | **No se piden** en la EV. | — |

---

## d) Requisito → tema de clase → técnica

| Requisito | Tema | Técnica usada |
|---|---|---|
| R8 estructura MVVM | 2.1.1, G7, 2.2.3 | Carpetas `ui/`, `viewmodel/`, `data/model`, `data/repository` (+ `validation/`, `nativo/`) |
| R1 pantallas | 2.1.3 | `@Composable`, `Text`, `Button`, `Image`, `Column`, `Row`, `Card`, `Modifier`, `Scaffold` |
| R2 alto contraste | 2.2.1 | `MaterialTheme` con `colorScheme` y `typography` propios (`Theme.kt`, `Color.kt`, `Type.kt`) |
| R3 teléfono y tablet | 2.2.1, 2.2.3 | `calculateWindowSizeClass`: `NavigationBar` (compacto) / `NavigationRail` (mediano/expandido) |
| R1 navegación | 2.2.3 | `NavController`, `NavHost`, rutas con argumentos, `TopAppBar` con "volver" |
| R4 formularios | 2.3.1 | `OutlinedTextField`, `RadioButton`, `Switch`, `Checkbox`, `DatePicker`, `FilterChip`, `Icon`, `Button`/`OutlinedButton`/`TextButton` |
| R5/R6 validación | 2.3.1, 2.3.2 | `data class` UiState + errores, `ViewModel` con `StateFlow`, `collectAsState`; errores en campo, `AlertDialog`, `Snackbar` |
| R7 animaciones | 2.4.1 | `animateColorAsState`, `animateDpAsState`, `animateFloatAsState`, `AnimatedVisibility`, `AnimatedContent`, `Crossfade`, loaders, transiciones del `NavHost` |
| Estado en pantalla | 2.4.1 | `remember`, `mutableStateOf`, `rememberSaveable` |
| R25 preferencias | 2.4.1 | **DataStore Preferences** |
| R9/R17 hitos | 2.5.1 | **Room**: `@Entity`, `@Dao` (CRUD), `@Database`, consultas con `Flow` |
| R13 cámara y galería | 2.4.3 | Permiso en Manifest + en tiempo de ejecución, **ActivityResult API** (`TakePicture`, `GetContent`), `FileProvider`, manejo de permiso negado |
| R14 notificaciones | 2.4.3 (pasos generales) | `NotificationManager` + permiso `POST_NOTIFICATIONS`. Sin WorkManager/AlarmManager: se revisa al abrir la app |

**Librerías agregadas (todas vistas en clase):** Navigation Compose, lifecycle-viewmodel-compose, Room (+ KSP),
DataStore Preferences, material3-window-size-class.
**Quitadas:** Retrofit y Gson (no se usaban).
**No agregadas por decisión del equipo:** `material-icons-extended` → los íconos son vectores XML en `res/drawable/ic_*.xml`.

---

## e) Arquitectura final

```
cl.duoc.tutorvirtual/
├── MainActivity.kt          ← arranca la app, calcula tamaño de pantalla y aplica el tema
├── TutorApp.kt              ← Application + ContenedorApp (crea los repositorios una vez)
├── data/
│   ├── model/               ← EtapaMetodologica, PreguntaFrecuente, Hito, Perfil (+ reglas de plazos)
│   ├── local/room/          ← HitoEntity, HitoDao, AppDatabase (con datos iniciales)
│   ├── local/datastore/     ← archivo DataStore y sus claves
│   └── repository/          ← GuiaRepository, FaqRepository, HitoRepository, PreferenciasRepository
├── validation/              ← HitoValidator, PerfilValidator (Kotlin puro, probados con JUnit)
├── viewmodel/               ← Inicio, Guia, Faq, Hitos, HitoForm, Perfil
├── nativo/                  ← GestorFotos (cámara/galería), Notificador
└── ui/
    ├── TutorVirtualApp.kt   ← Scaffold + TopAppBar + barra de navegación adaptable
    ├── navigation/          ← Rutas, AppNavHost
    ├── components/          ← CampoValidado, TarjetaExpandible, ChipEstado, ProgresoAnimado, SelectorFoto…
    ├── screens/             ← Inicio, Guia, Faq, Hitos, HitoForm, Perfil
    └── theme/               ← colores, tipografía, tema
```

---

## f) Plan de implementación — estado

| Etapa | Qué se hizo | Commit |
|---|---|---|
| 0 | Dependencias de clase, quitar Retrofit, `.gitignore` | `6d51f49` ✅ |
| 1 | Tema alto contraste + íconos vectoriales | `d6e2d6b` ✅ |
| 2 | Navegación con NavHost y barra adaptable | `ad6e706` ✅ |
| 3 | Guía metodológica + pautas | `929683e` ✅ |
| 4 | Buscador FAQ | `08ee1b6` ✅ |
| 5 | Room + lista de hitos con checklist | `deeb81b` ✅ |
| 6 | Formulario de hito con validación desacoplada | `a0cfec1` ✅ |
| 7 | Perfil + DataStore + Inicio | `7fa48c7` ✅ |
| 8 | Cámara + galería | `372835d` ✅ |
| 9 | Notificaciones locales | `4a740ca` ✅ |
| 10 | Transiciones y animaciones extra | `0f7dcd4` ✅ |
| 11 | Pruebas unitarias (25) | `8a85a5f` ✅ |
| 12 | PLAN, README, DEFENSA | este commit ✅ |

### Pendientes (no son código)

- [ ] Escribir los **nombres** del equipo en `README.md`.
- [ ] Que **la pareja** haga commits propios (por ejemplo: cambiar contenido de la guía o FAQ con material real).
- [ ] Crear **issues** en GitHub y cerrarlos con los commits/PR.
- [ ] Crear tablero **Trello** (Por hacer / En curso / Finalizado) con las etapas repartidas.
- [ ] Hacer merge de `development` a `main` antes de la fecha y verificar que el repo sea **público** con acceso al docente.
- [ ] Subir el código al **AVA grupal** y activar el **AVA individual**.
- [ ] Tomar capturas para el README (ver lista en `README.md`).

---

## g) Dudas para el profesor

Resueltas por el equipo:
- ~~¿Repo público o privado?~~ → **Público**.
- ~~¿Librerías no vistas en clase?~~ → **Evitarlas** por ahora (sin `material-icons-extended`, WorkManager ni AlarmManager).
- ~~¿Notificaciones cuentan como recurso nativo?~~ → **No se cuentan** por ahora; los 2 recursos son cámara y galería.

Aún por preguntar:
1. **Fecha y hora exacta** de entrega.
2. ¿**Cámara y galería** cuentan como **dos** recursos nativos distintos? (Si no, habría que sumar otro, por ejemplo GPS o contar las notificaciones.)
3. ¿Se acepta que los recordatorios se revisen **al abrir la app**, o se espera una alarma programada (WorkManager/AlarmManager)?
4. **Perfil profesor guía:** ¿basta el rol simple sin login para este avance?
5. **Contenido real:** ¿hay material autorizado de ESUCAR para guía, FAQ y pautas? (Hoy es contenido de ejemplo.)
6. ¿Basta con que todo funcione **offline** (sin sincronizar con internet)?
7. ¿Está permitido usar **asistentes de IA** y hay que declararlo? (Los commits llevan "Co-Authored-By: Claude".)
