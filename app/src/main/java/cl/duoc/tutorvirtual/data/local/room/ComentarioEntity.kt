package cl.duoc.tutorvirtual.data.local.room

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import cl.duoc.tutorvirtual.data.model.Adjunto
import cl.duoc.tutorvirtual.data.model.Comentario
import cl.duoc.tutorvirtual.data.model.Rol
import cl.duoc.tutorvirtual.data.model.TipoComentario
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Tabla "comentarios": conversación de cada hito.
 * ForeignKey + CASCADE: si se borra el hito, se borran sus comentarios.
 */
@Entity(
    tableName = "comentarios",
    foreignKeys = [ForeignKey(
        entity = HitoEntity::class,
        parentColumns = ["id"],
        childColumns = ["hitoId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("hitoId")]
)
data class ComentarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hitoId: Long,
    val autorRol: String,
    val autorNombre: String,
    val tipo: String,
    val texto: String,
    val fechaHora: Long,            // milisegundos
    val archivoRuta: String?,
    val archivoNombre: String?
)

/** Resultado de la consulta de resumen (cuántos mensajes y quién escribió último). */
data class ResumenComentariosFila(val hitoId: Long, val total: Int, val ultimoRol: String?)

fun ComentarioEntity.aModelo() = Comentario(
    id = id,
    hitoId = hitoId,
    autorRol = Rol.entries.find { it.name == autorRol } ?: Rol.ESTUDIANTE,
    autorNombre = autorNombre,
    tipo = TipoComentario.entries.find { it.name == tipo } ?: TipoComentario.AVANCE,
    texto = texto,
    fechaHora = LocalDateTime.ofInstant(Instant.ofEpochMilli(fechaHora), ZoneId.systemDefault()),
    adjunto = if (archivoRuta != null) Adjunto(archivoRuta, archivoNombre ?: "archivo") else null
)

fun Comentario.aEntidad() = ComentarioEntity(
    id = id,
    hitoId = hitoId,
    autorRol = autorRol.name,
    autorNombre = autorNombre,
    tipo = tipo.name,
    texto = texto,
    fechaHora = fechaHora.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
    archivoRuta = adjunto?.ruta,
    archivoNombre = adjunto?.nombre
)
