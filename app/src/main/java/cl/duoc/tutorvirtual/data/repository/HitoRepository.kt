package cl.duoc.tutorvirtual.data.repository

import cl.duoc.tutorvirtual.data.local.room.ComentarioDao
import cl.duoc.tutorvirtual.data.local.room.ComentarioEntity
import cl.duoc.tutorvirtual.data.local.room.HitoDao
import cl.duoc.tutorvirtual.data.local.room.aEntidad
import cl.duoc.tutorvirtual.data.local.room.aModelo
import cl.duoc.tutorvirtual.data.model.Comentario
import cl.duoc.tutorvirtual.data.model.EstadoHito
import cl.duoc.tutorvirtual.data.model.Hito
import cl.duoc.tutorvirtual.data.model.ResumenConversacion
import cl.duoc.tutorvirtual.data.model.Rol
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repositorio de hitos y de sus comentarios: único punto por el que los ViewModels acceden a Room.
 * Si mañana los hitos vienen de un servidor, solo cambia esta clase.
 */
class HitoRepository(
    private val dao: HitoDao,
    private val comentarios: ComentarioDao
) {

    fun observarHitos(): Flow<List<Hito>> =
        dao.observarTodos().map { lista -> lista.map { it.aModelo() } }

    fun observarHito(id: Long): Flow<Hito?> = dao.observarPorId(id).map { it?.aModelo() }

    suspend fun obtener(id: Long): Hito? = dao.obtenerPorId(id)?.aModelo()

    /** Crea el hito si es nuevo (id = 0) o lo actualiza si ya existe. */
    suspend fun guardar(hito: Hito): Long =
        if (hito.id == 0L) dao.insertar(hito.aEntidad())
        else { dao.actualizar(hito.aEntidad()); hito.id }

    suspend fun cambiarEstado(hito: Hito, nuevo: EstadoHito) =
        dao.actualizar(hito.copy(estado = nuevo).aEntidad())

    /**
     * Elimina el hito (sus comentarios se borran en cascada).
     * Devuelve una copia de los comentarios para poder "Deshacer".
     */
    suspend fun eliminar(hito: Hito): List<ComentarioEntity> {
        val copia = comentarios.listarDeHito(hito.id)
        dao.eliminar(hito.aEntidad())
        return copia
    }

    /** Vuelve a insertar un hito eliminado junto con sus comentarios (botón "Deshacer"). */
    suspend fun restaurar(hito: Hito, conversacion: List<ComentarioEntity> = emptyList()) {
        dao.insertar(hito.aEntidad())
        if (conversacion.isNotEmpty()) comentarios.insertarTodos(conversacion)
    }

    // ---------- Comentarios (avances, dudas y respuestas) ----------

    fun observarComentarios(hitoId: Long): Flow<List<Comentario>> =
        comentarios.observarDeHito(hitoId).map { lista -> lista.map { it.aModelo() } }

    /** Mapa hitoId → resumen de su conversación. */
    fun observarResumenComentarios(): Flow<Map<Long, ResumenConversacion>> =
        comentarios.observarResumen().map { filas ->
            filas.associate { f ->
                f.hitoId to ResumenConversacion(f.total, Rol.entries.find { it.name == f.ultimoRol })
            }
        }

    suspend fun agregarComentario(comentario: Comentario): Long = comentarios.insertar(comentario.aEntidad())

    suspend fun eliminarComentario(id: Long) = comentarios.eliminar(id)
}
