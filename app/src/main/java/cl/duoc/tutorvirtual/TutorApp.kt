package cl.duoc.tutorvirtual

import android.app.Application
import android.content.Context
import cl.duoc.tutorvirtual.data.local.datastore.dataStore
import cl.duoc.tutorvirtual.data.local.room.AppDatabase
import cl.duoc.tutorvirtual.data.repository.HitoRepository
import cl.duoc.tutorvirtual.data.repository.PreferenciasRepository

/**
 * Clase Application: existe una sola vez mientras la app está abierta.
 * Guarda el "contenedor" con los repositorios para que todos los ViewModels
 * usen la misma base de datos y el mismo DataStore.
 */
class TutorApp : Application() {
    val contenedor: ContenedorApp by lazy { ContenedorApp(this) }
}

/** Crea los repositorios una sola vez (inyección de dependencias manual y simple). */
class ContenedorApp(context: Context) {
    val hitoRepository: HitoRepository by lazy {
        val db = AppDatabase.obtener(context)
        HitoRepository(db.hitoDao(), db.comentarioDao())
    }
    val preferencias: PreferenciasRepository by lazy {
        PreferenciasRepository(context.dataStore)
    }
}
