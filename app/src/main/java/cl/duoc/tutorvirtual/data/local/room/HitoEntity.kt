package cl.duoc.tutorvirtual.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import cl.duoc.tutorvirtual.data.model.EstadoHito
import cl.duoc.tutorvirtual.data.model.Hito
import java.time.LocalDate

/**
 * Tabla "hitos" de SQLite (2.5.1 Room: @Entity).
 * La fecha se guarda como número de día (epochDay) y el estado como texto,
 * así SQLite puede guardarlos sin conversores especiales.
 */
@Entity(tableName = "hitos")
data class HitoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val titulo: String,
    val descripcion: String,
    val formatoRequerido: String,
    val fechaLimiteEpochDay: Long,
    val estado: String,
    val fotoEvidencia: String?
)

// Conversiones entre la tabla y el modelo de la app
fun HitoEntity.aModelo() = Hito(
    id = id,
    titulo = titulo,
    descripcion = descripcion,
    formatoRequerido = formatoRequerido,
    fechaLimite = LocalDate.ofEpochDay(fechaLimiteEpochDay),
    estado = runCatching { EstadoHito.valueOf(estado) }.getOrDefault(EstadoHito.POR_INICIAR),
    fotoEvidencia = fotoEvidencia
)

fun Hito.aEntidad() = HitoEntity(
    id = id,
    titulo = titulo,
    descripcion = descripcion,
    formatoRequerido = formatoRequerido,
    fechaLimiteEpochDay = fechaLimite.toEpochDay(),
    estado = estado.name,
    fotoEvidencia = fotoEvidencia
)
