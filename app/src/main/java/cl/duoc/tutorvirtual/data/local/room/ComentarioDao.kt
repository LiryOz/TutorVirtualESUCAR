package cl.duoc.tutorvirtual.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** Operaciones sobre la tabla "comentarios". */
@Dao
interface ComentarioDao {

    @Query("SELECT * FROM comentarios WHERE hitoId = :hitoId ORDER BY fechaHora ASC, id ASC")
    fun observarDeHito(hitoId: Long): Flow<List<ComentarioEntity>>

    @Query("SELECT * FROM comentarios WHERE hitoId = :hitoId")
    suspend fun listarDeHito(hitoId: Long): List<ComentarioEntity>

    /** Por cada hito: total de mensajes y el rol de quien escribió el último. */
    @Query(
        """
        SELECT c.hitoId AS hitoId, COUNT(*) AS total,
               (SELECT u.autorRol FROM comentarios u WHERE u.hitoId = c.hitoId
                ORDER BY u.fechaHora DESC, u.id DESC LIMIT 1) AS ultimoRol
        FROM comentarios c GROUP BY c.hitoId
        """
    )
    fun observarResumen(): Flow<List<ResumenComentariosFila>>

    @Insert
    suspend fun insertar(comentario: ComentarioEntity): Long

    @Insert
    suspend fun insertarTodos(comentarios: List<ComentarioEntity>)

    @Query("DELETE FROM comentarios WHERE id = :id")
    suspend fun eliminar(id: Long)
}
