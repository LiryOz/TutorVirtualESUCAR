package cl.duoc.tutorvirtual

import android.app.Application
import android.content.Context
import cl.duoc.tutorvirtual.data.local.room.AppDatabase
import cl.duoc.tutorvirtual.data.repository.HitoRepository

/**
 * Clase Application: existe una sola vez mientras la app está abierta.
 * Guarda el "contenedor" con los repositorios para que todos los ViewModels
 * usen la misma base de datos.
 */
class TutorApp : Application() {
    val contenedor: ContenedorApp by lazy { ContenedorApp(this) }
}

/** Crea los repositorios una sola vez (inyección de dependencias manual y simple). */
class ContenedorApp(context: Context) {
    val hitoRepository: HitoRepository by lazy {
        HitoRepository(AppDatabase.obtener(context).hitoDao())
    }
}
