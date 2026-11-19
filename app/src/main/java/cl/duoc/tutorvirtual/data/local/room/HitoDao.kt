package cl.duoc.tutorvirtual.data.local.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO = las operaciones CRUD sobre la tabla "hitos" (2.5.1).
 * Room genera el código SQL real al compilar (por eso usamos KSP).
 */
@Dao
interface HitoDao {

    // READ: Flow emite una lista nueva cada vez que la tabla cambia
    @Query("SELECT * FROM hitos ORDER BY fechaLimiteEpochDay ASC")
    fun observarTodos(): Flow<List<HitoEntity>>

    @Query("SELECT * FROM hitos WHERE id = :id")
    suspend fun obtenerPorId(id: Long): HitoEntity?

    // Un solo hito, que se actualiza solo cuando cambia (pantalla de detalle)
    @Query("SELECT * FROM hitos WHERE id = :id")
    fun observarPorId(id: Long): Flow<HitoEntity?>

    // CREATE
    @Insert
    suspend fun insertar(hito: HitoEntity): Long

    // UPDATE
    @Update
    suspend fun actualizar(hito: HitoEntity)

    // DELETE
    @Delete
    suspend fun eliminar(hito: HitoEntity)
}
