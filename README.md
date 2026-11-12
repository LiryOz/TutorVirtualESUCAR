# Tutor Virtual ESUCAR

Aplicación Android (Kotlin + Jetpack Compose) que funciona como **tutor virtual para la asignatura Trabajo de
Título I, II y III** de la Escuela de Suboficiales de Carabineros (ESUCAR).

Proyecto de la **Evaluación Parcial 2** de DSY1105 – Desarrollo de Aplicaciones Móviles (Duoc UC).

## Equipo

| Integrante | GitHub |
|---|---|
| _Nombre Apellido_ | _usuario_ |
| _Nombre Apellido_ | _usuario_ |

> Completar con los nombres reales antes de la entrega.

## ¿Qué problema resuelve?

Los estudiantes de Trabajo de Título avanzan de forma autónoma con material estático y no tienen una herramienta en el
celular para resolver dudas rápidas de metodología, revisar pautas o recordar sus fechas de entrega. Por eso
los profesores guía reciben muchas consultas repetidas.

La app reúne en un solo lugar la **guía metodológica paso a paso**, un **buscador de preguntas frecuentes**, las
**pautas de evaluación** y el **seguimiento de hitos** con recordatorios. Todo funciona **sin internet**.

## Cuentas de prueba

| Perfil | Usuario | Contraseña |
|---|---|---|
| Estudiante de trabajo de título | `alumno` | `alumno123` |
| Profesor guía | `profesor` | `profesor123` |

En el login se elige el perfil y el botón **Usar** rellena la cuenta de prueba. Una cuenta solo entra con su propio perfil.

## Funcionalidades

| Módulo | Qué hace |
|---|---|
| **Inicio de sesión** | Login con dos perfiles (Estudiante / Profesor guía), cuentas de prueba, mostrar/ocultar contraseña, errores animados y sesión guardada en DataStore. Botón "Cerrar sesión" en la barra superior y en Perfil. |
| **Inicio** | Encabezado animado con el perfil activo. Estudiante: alertas de plazos, barra de avance, próxima entrega y accesos rápidos. Profesor guía: indicadores (por revisar, aprobados, vencidos) y lista de entregas "Enviadas" para aprobar. |
| **Guía metodológica** | 6 etapas (Planteamiento, Marco teórico, Metodología, Recogida de información, Análisis, Conclusiones) con paso a paso y **pauta de evaluación** desplegable. Botones Anterior/Siguiente. |
| **Sargento (asistente)** | Chat con el Sargento, perro de Carabineros de Chile: responde las preguntas del banco aunque se escriban con otras palabras (palabras clave, sin tildes), sugiere preguntas relacionadas y, si la consulta está fuera de contexto, responde "No tengo respuesta para eso". Funciona sin conexión. |
| **Banco de preguntas** | (Desde el chat del Sargento) Buscador que ignora mayúsculas y tildes, más filtro por categoría (Citación, Metodología, Formato, Entregas). |
| **Mis hitos** | Lista de entregas guardadas en el teléfono (Room) con estado, fecha límite, días restantes y contador de comentarios. **Profesor guía:** crea, edita y elimina hitos (con "Deshacer"). **Estudiante:** marca sus entregas con el checklist. |
| **Detalle del hito** | Estado con permisos por perfil (solo el profesor guía aprueba). Conversación por hito: el estudiante comenta **avances o dudas** y **adjunta archivos** (PDF, Word, imágenes; máx. 10 MB); el profesor guía **responde**. |
| **Formulario de hito** | (Solo profesor guía) Crear y editar hitos: título, descripción, formato requerido, fecha (calendario), estado y **foto de evidencia**. Validación por campo con íconos y mensajes. |
| **Perfil** | Perfil fijado por la cuenta (estudiante / profesor guía), nombre, RUT (con dígito verificador), correo, asignatura y profesor guía. **Foto de perfil** con cámara o galería. |
| **Ajustes** | Recordatorios de fechas límite, **modo oscuro** (la app es clara por defecto) y **tamaño de letra** (Pequeño / Normal / Grande), guardados con DataStore. |
| **Notificaciones** | Al abrir la app, avisa si hay hitos vencidos o que vencen en 3 días o menos. |

### Requisitos de la evaluación y dónde se cumplen

| Requisito | Implementación |
|---|---|
| Interfaz organizada y navegación clara | `NavHost` + `TopAppBar` + `NavigationBar` (teléfono) / `NavigationRail` (tablet) |
| Formularios validados con íconos y mensajes | `CampoValidado`: borde rojo, ícono de error u "ok", mensaje y contador |
| Validaciones desde la lógica | `validation/HitoValidator.kt` y `PerfilValidator.kt` (Kotlin puro) usados por los ViewModels |
| Animaciones funcionales | Color de estado, progreso, tarjetas expandibles, loaders, transiciones entre pantallas |
| Estructura modular + persistencia local | MVVM; **Room** (hitos) y **DataStore** (perfil y ajustes) |
| Dos recursos nativos | **Cámara** (`TakePicture` + permiso en tiempo de ejecución) y **Galería** (`GetContent`) |

## Tecnologías

- Kotlin, Jetpack Compose, Material 3
- Arquitectura **MVVM** (ViewModel + StateFlow)
- Navigation Compose
- Room (SQLite) con KSP
- DataStore Preferences
- Material 3 Window Size Classes
- ActivityResult API (cámara, galería y permisos)
- JUnit 4 (pruebas unitarias)

No se usan librerías externas fuera de las vistas en clase. Los íconos son vectores XML propios (`res/drawable/ic_*.xml`).

## Cómo ejecutar

1. Instalar **Android Studio** (versión reciente) con el **SDK de Android 37**.
2. Clonar el repositorio:
   ```bash
   git clone https://github.com/LiryOz/TutorVirtualESUCAR.git
   ```
3. Abrir la carpeta del proyecto en Android Studio y esperar la sincronización de Gradle.
   Gradle descarga automáticamente el JDK que necesita.
4. Crear un emulador en **Device Manager** (Android 8.0 / API 26 o superior) o conectar un teléfono con depuración USB.
5. Presionar **Run ▶** (configuración `app`).

Desde la terminal:

```bash
./gradlew assembleDebug        # compilar el APK de prueba
./gradlew testDebugUnitTest    # ejecutar las 25 pruebas unitarias
```

> La primera vez que se abre, la app carga 4 hitos de ejemplo y pide permiso de notificaciones (Android 13+).
> El permiso de cámara se pide recién al tocar "Cámara".

## Estructura del proyecto

```
app/src/main/java/cl/duoc/tutorvirtual/
├── MainActivity.kt / TutorApp.kt   ← arranque y creación de repositorios
├── data/model/                     ← modelos (Hito, Perfil, EtapaMetodologica, PreguntaFrecuente)
├── data/local/room/                ← base de datos Room (entidad, DAO, base)
├── data/local/datastore/           ← DataStore de preferencias
├── data/repository/                ← repositorios (única puerta a los datos)
├── validation/                     ← reglas de validación
├── viewmodel/                      ← lógica de presentación (StateFlow)
├── nativo/                         ← cámara/galería y notificaciones
└── ui/                             ← pantallas, componentes, navegación y tema
```

## Capturas sugeridas

Tomar estas capturas del emulador y guardarlas en `docs/capturas/`:

1. **Inicio** con la tarjeta roja "Atención con tus plazos" y la barra de avance.
2. **Guía**: lista de etapas y detalle con la pauta desplegada.
3. **Preguntas frecuentes** filtrando por "Citación".
4. **Mis hitos** con distintos estados (colores) y un hito marcado como entregado.
5. **Formulario de hito** con errores en rojo (presionar "Crear hito" con campos vacíos).
6. **Calendario** (DatePicker) abierto.
7. **Perfil** con RUT inválido mostrando "El dígito verificador no es válido".
8. **Permiso de cámara** pedido por el sistema y mensaje cuando se niega.
9. **Foto** tomada con la cámara mostrada en el perfil.
10. **Notificación** "Tienes entregas próximas" en la barra de notificaciones.
11. **Modo horizontal / tablet** mostrando la barra lateral (NavigationRail).

## Notas

- El contenido de la guía, las preguntas frecuentes y las pautas es **de ejemplo**; debe reemplazarse por el material
  autorizado de ESUCAR (`GuiaRepository.kt` y `FaqRepository.kt`).
- Las cuentas son genéricas y viven dentro de la app (`data/model/Cuenta.kt`), porque el MVP no se conecta a servidores de la institución (caso 3.4).
- Más detalles en [`PLAN.md`](PLAN.md) (requisitos y estado) y [`DEFENSA.md`](DEFENSA.md) (explicación del código).
