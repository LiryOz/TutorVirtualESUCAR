package cl.duoc.tutorvirtual.data.local.room

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import cl.duoc.tutorvirtual.data.model.EstadoHito
import java.time.LocalDate

/** Base de datos local SQLite de la app (2.5.1 Room: @Database). */
@Database(entities = [HitoEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun hitoDao(): HitoDao

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        /** Una sola instancia para toda la app (patrón singleton). */
        fun obtener(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tutor_virtual.db"
                )
                    .addCallback(DatosIniciales)
                    .build()
                    .also { instancia = it }
            }
    }

    /**
     * Se ejecuta UNA vez, cuando la base se crea por primera vez:
     * carga los hitos típicos del Trabajo de Título para que la app no parta vacía.
     */
    private object DatosIniciales : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            val hoy = LocalDate.now()
            listOf(
                Triple("Avance 1: Planteamiento del problema", 5L, EstadoHito.EN_DESARROLLO),
                Triple("Avance 2: Marco teórico", 20L, EstadoHito.POR_INICIAR),
                Triple("Avance 3: Metodología", 35L, EstadoHito.POR_INICIAR),
                Triple("Informe final", 60L, EstadoHito.POR_INICIAR)
            ).forEach { (titulo, dias, estado) ->
                val valores = ContentValues().apply {
                    put("titulo", titulo)
                    put("descripcion", "Entrega definida por el profesor guía para esta etapa del trabajo de título.")
                    put("formatoRequerido", "Documento Word o PDF, norma APA 7")
                    put("fechaLimiteEpochDay", hoy.plusDays(dias).toEpochDay())
                    put("estado", estado.name)
                    putNull("fotoEvidencia")
                }
                db.insert("hitos", SQLiteDatabase.CONFLICT_REPLACE, valores)
            }
        }
    }
}
