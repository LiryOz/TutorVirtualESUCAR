package cl.duoc.tutorvirtual.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import cl.duoc.tutorvirtual.data.local.datastore.Claves
import cl.duoc.tutorvirtual.data.model.Ajustes
import cl.duoc.tutorvirtual.data.model.Asignatura
import cl.duoc.tutorvirtual.data.model.Cuenta
import cl.duoc.tutorvirtual.data.model.Perfil
import cl.duoc.tutorvirtual.data.model.Rol
import cl.duoc.tutorvirtual.data.model.TamanoTexto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/** Lee y guarda el perfil y los ajustes en DataStore. */
class PreferenciasRepository(private val dataStore: DataStore<Preferences>) {

    /** Flow: emite el perfil cada vez que cambia algo guardado. */
    val perfil: Flow<Perfil> = dataStore.data.map { p ->
        Perfil(
            nombre = p[Claves.NOMBRE] ?: "",
            rut = p[Claves.RUT] ?: "",
            correo = p[Claves.CORREO] ?: "",
            // Se guardó el NOMBRE del enum (ej: "TT1"); aquí lo convertimos de vuelta
            asignatura = Asignatura.entries.find { it.name == p[Claves.ASIGNATURA] },
            profesorGuia = p[Claves.PROFESOR_GUIA] ?: "",
            rol = Rol.entries.find { it.name == p[Claves.ROL] } ?: Rol.ESTUDIANTE,
            foto = p[Claves.FOTO]
        )
    }

    val ajustes: Flow<Ajustes> = dataStore.data.map { p ->
        Ajustes(
            recordatorios = p[Claves.RECORDATORIOS] ?: true,
            // Si viene de la versión anterior (sí/no), "sí" equivale a Grande
            tamanoTexto = TamanoTexto.entries.find { it.name == p[Claves.TAMANO_TEXTO] }
                ?: if (p[Claves.TEXTO_GRANDE] == true) TamanoTexto.GRANDE else TamanoTexto.NORMAL,
            modoOscuro = p[Claves.MODO_OSCURO] ?: false
        )
    }.distinctUntilChanged()

    val esProfesor: Flow<Boolean> = perfil.map { it.rol == Rol.PROFESOR_GUIA }

    suspend fun guardarPerfil(perfil: Perfil) {
        dataStore.edit { p ->
            p[Claves.NOMBRE] = perfil.nombre
            p[Claves.RUT] = perfil.rut
            p[Claves.CORREO] = perfil.correo
            perfil.asignatura?.let { p[Claves.ASIGNATURA] = it.name }
            p[Claves.PROFESOR_GUIA] = perfil.profesorGuia
            p[Claves.ROL] = perfil.rol.name
        }
    }

    suspend fun guardarFoto(ruta: String?) {
        dataStore.edit { p -> if (ruta == null) p.remove(Claves.FOTO) else p[Claves.FOTO] = ruta }
    }

    /** Usuario con sesión abierta; "" si nadie ha iniciado sesión. */
    val sesion: Flow<String> = dataStore.data.map { it[Claves.SESION] ?: "" }.distinctUntilChanged()

    /**
     * Abre la sesión. Si entra una cuenta distinta a la anterior, el perfil se
     * reemplaza con los datos de esa cuenta. Devuelve la foto que quedó sin dueño
     * (para borrar el archivo) o null.
     */
    suspend fun iniciarSesion(cuenta: Cuenta): String? {
        var fotoDescartada: String? = null
        dataStore.edit { p ->
            if (p[Claves.ULTIMA_CUENTA] != cuenta.usuario) {
                fotoDescartada = p[Claves.FOTO]
                p.remove(Claves.FOTO)
                p.remove(Claves.RUT)
                p.remove(Claves.ASIGNATURA)
                p[Claves.NOMBRE] = cuenta.nombre
                p[Claves.CORREO] = cuenta.correo
                p[Claves.PROFESOR_GUIA] = cuenta.profesorGuia
                cuenta.asignatura?.let { p[Claves.ASIGNATURA] = it.name }
            }
            p[Claves.ROL] = cuenta.rol.name
            p[Claves.ULTIMA_CUENTA] = cuenta.usuario
            p[Claves.SESION] = cuenta.usuario
        }
        return fotoDescartada
    }

    /** Cierra la sesión; el perfil se conserva para el próximo ingreso de la misma cuenta. */
    suspend fun cerrarSesion() {
        dataStore.edit { it.remove(Claves.SESION) }
    }

    suspend fun cambiarRecordatorios(activo: Boolean) {
        dataStore.edit { it[Claves.RECORDATORIOS] = activo }
    }

    suspend fun cambiarTamanoTexto(tamano: TamanoTexto) {
        dataStore.edit { it[Claves.TAMANO_TEXTO] = tamano.name }
    }

    suspend fun cambiarModoOscuro(activo: Boolean) {
        dataStore.edit { it[Claves.MODO_OSCURO] = activo }
    }
}
