package cl.duoc.tutorvirtual.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

/**
 * Archivo DataStore "preferencias" (2.4.1): guarda datos simples clave-valor
 * de forma asíncrona y persistente (reemplazo moderno de SharedPreferences).
 */
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "preferencias")

/** Nombres (claves) de cada dato guardado. */
object Claves {
    val NOMBRE = stringPreferencesKey("nombre")
    val RUT = stringPreferencesKey("rut")
    val CORREO = stringPreferencesKey("correo")
    val ASIGNATURA = stringPreferencesKey("asignatura")
    val PROFESOR_GUIA = stringPreferencesKey("profesor_guia")
    val ROL = stringPreferencesKey("rol")
    val FOTO = stringPreferencesKey("foto")
    val RECORDATORIOS = booleanPreferencesKey("recordatorios")
    val TEXTO_GRANDE = booleanPreferencesKey("texto_grande")      // versión anterior (sí/no)
    val TAMANO_TEXTO = stringPreferencesKey("tamano_texto")       // PEQUENO / NORMAL / GRANDE
    val MODO_OSCURO = booleanPreferencesKey("modo_oscuro")
    val SESION = stringPreferencesKey("sesion_usuario")          // cuenta con sesión abierta
    val ULTIMA_CUENTA = stringPreferencesKey("ultima_cuenta")   // de quién son los datos del perfil
}
