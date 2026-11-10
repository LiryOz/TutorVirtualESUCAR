package cl.duoc.tutorvirtual.data.repository

import cl.duoc.tutorvirtual.data.local.room.HitoDao
import cl.duoc.tutorvirtual.data.local.room.aEntidad
import cl.duoc.tutorvirtual.data.local.room.aModelo
import cl.duoc.tutorvirtual.data.model.EstadoHito
import cl.duoc.tutorvirtual.data.model.Hito
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repositorio de hitos: único punto por el que los ViewModels acceden a Room.
 * Si mañana los hitos vienen de un servidor, solo cambia esta clase.
 */
class HitoRepository(private val dao: HitoDao) {

    fun observarHitos(): Flow<List<Hito>> =
        dao.observarTodos().map { lista -> lista.map { it.aModelo() } }

    suspend fun obtener(id: Long): Hito? = dao.obtenerPorId(id)?.aModelo()

    /** Crea el hito si es nuevo (id = 0) o lo actualiza si ya existe. */
    suspend fun guardar(hito: Hito): Long =
        if (hito.id == 0L) dao.insertar(hito.aEntidad())
        else { dao.actualizar(hito.aEntidad()); hito.id }

    suspend fun cambiarEstado(hito: Hito, nuevo: EstadoHito) =
        dao.actualizar(hito.copy(estado = nuevo).aEntidad())

    suspend fun eliminar(hito: Hito) = dao.eliminar(hito.aEntidad())

    /** Vuelve a insertar un hito eliminado (para el botón "Deshacer"). */
    suspend fun restaurar(hito: Hito) { dao.insertar(hito.aEntidad()) }
}
