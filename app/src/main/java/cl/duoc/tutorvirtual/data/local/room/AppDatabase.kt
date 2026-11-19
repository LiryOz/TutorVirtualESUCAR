package cl.duoc.tutorvirtual.data.local.room

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.time.LocalDate

/** Base de datos local SQLite de la app (2.5.1 Room: @Database). */
@Database(entities = [HitoEntity::class, ComentarioEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun hitoDao(): HitoDao
    abstract fun comentarioDao(): ComentarioDao

    companion object {
        /**
         * Migración 1 → 2: agrega la tabla de comentarios SIN borrar los hitos ya guardados.
         * (El SQL es el mismo que Room generaría para ComentarioEntity.)
         */
        private val MIGRACION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `comentarios` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `hitoId` INTEGER NOT NULL, " +
                        "`autorRol` TEXT NOT NULL, `autorNombre` TEXT NOT NULL, `tipo` TEXT NOT NULL, " +
                        "`texto` TEXT NOT NULL, `fechaHora` INTEGER NOT NULL, `archivoRuta` TEXT, `archivoNombre` TEXT, " +
                        "FOREIGN KEY(`hitoId`) REFERENCES `hitos`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_comentarios_hitoId` ON `comentarios` (`hitoId`)")
            }
        }

        // Guardamos la base creada para no abrirla dos veces
        private var instancia: AppDatabase? = null

        /** Devuelve siempre la MISMA base de datos (se crea solo la primera vez). */
        fun obtener(context: Context): AppDatabase {
            if (instancia == null) {
                instancia = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tutor_virtual.db"           // nombre del archivo dentro del teléfono
                )
                    .addCallback(DatosIniciales)
                    .addMigrations(MIGRACION_1_2)
                    .build()
            }
            return instancia!!
        }
    }

    /**
     * Se ejecuta UNA sola vez, cuando la base se crea por primera vez:
     * carga 4 hitos de ejemplo para que la app no parta vacía.
     */
    private object DatosIniciales : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            val hoy = LocalDate.now()
            agregarHito(db, "Avance 1: Planteamiento del problema", hoy.plusDays(5), "EN_DESARROLLO")
            agregarHito(db, "Avance 2: Marco teórico", hoy.plusDays(20), "POR_INICIAR")
            agregarHito(db, "Avance 3: Metodología", hoy.plusDays(35), "POR_INICIAR")
            agregarHito(db, "Informe final", hoy.plusDays(60), "POR_INICIAR")
        }

        /** Inserta una fila en la tabla "hitos" (las claves son los nombres de las columnas). */
        private fun agregarHito(db: SupportSQLiteDatabase, titulo: String, fecha: LocalDate, estado: String) {
            val fila = ContentValues()
            fila.put("titulo", titulo)
            fila.put("descripcion", "Entrega definida por el profesor guía para esta etapa del trabajo de título.")
            fila.put("formatoRequerido", "Documento Word o PDF, norma APA 7")
            fila.put("fechaLimiteEpochDay", fecha.toEpochDay())
            fila.put("estado", estado)
            fila.putNull("fotoEvidencia")
            db.insert("hitos", SQLiteDatabase.CONFLICT_REPLACE, fila)
        }
    }
}
