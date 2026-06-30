package es.uib.record.backend.publications.domain.model

import java.util.UUID

/**
 * Autor de una publicación. Puede ser interno (un usuario registrado, referenciado por
 * [InternalAuthor.userId]) o externo (alguien que participó pero aún no tiene cuenta, representado
 * por su nombre en [ExternalAuthor]).
 *
 * Cada autor tiene una identidad propia [id] (asignada al persistir) que permite localizarlo, p.
 * ej. para convertir un externo en interno en el futuro.
 */
sealed interface PublicationAuthor {
    val id: UUID?

    data class InternalAuthor(val userId: UUID, override val id: UUID? = null) : PublicationAuthor

    data class ExternalAuthor(
        val firstName: String,
        val lastName: String,
        override val id: UUID? = null,
    ) : PublicationAuthor
}

/** Devuelve el userId si el autor es interno, o null si es externo. */
fun PublicationAuthor.internalUserId(): UUID? = (this as? PublicationAuthor.InternalAuthor)?.userId
