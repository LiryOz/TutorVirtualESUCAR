package cl.duoc.tutorvirtual.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import cl.duoc.tutorvirtual.data.local.datastore.Claves
import cl.duoc.tutorvirtual.data.model.Ajustes
import cl.duoc.tutorvirtual.data.model.Asignatura
import cl.duoc.tutorvirtual.data.model.Perfil
import cl.duoc.tutorvirtual.data.model.Rol
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
            textoGrande = p[Claves.TEXTO_GRANDE] ?: false
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

    suspend fun cambiarRecordatorios(activo: Boolean) {
        dataStore.edit { it[Claves.RECORDATORIOS] = activo }
    }

    suspend fun cambiarTextoGrande(activo: Boolean) {
        dataStore.edit { it[Claves.TEXTO_GRANDE] = activo }
    }
}
