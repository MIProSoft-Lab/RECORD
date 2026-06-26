package es.uib.record.backend.publications.domain.model

import es.uib.record.backend.publications.domain.exception.DoiNotAllowedException
import java.time.Instant
import java.util.UUID

class Publication(
    val id: UUID? = null,
    val title: String,
    val abstractText: String? = null,
    val doi: String? = null,
    val journalId: UUID,
    val groupId: UUID,
    val status: PublicationStatus = PublicationStatus.PLANNED,
    val createdBy: UUID,
    val createdAt: Instant = Instant.now(),
    authors: List<PublicationAuthor> = emptyList(),
) {
    private val _authors: MutableList<PublicationAuthor> = authors.toMutableList()

    val authors: List<PublicationAuthor>
        get() = _authors.toList()

    init {
        // Invariante: el DOI solo puede tener valor cuando la publicación está PUBLISHED.
        if (!doi.isNullOrBlank() && status != PublicationStatus.PUBLISHED) {
            throw DoiNotAllowedException()
        }
    }

    /** Añade un co-autor interno al final; deduplica por usuario (no se repite). */
    fun addInternalAuthor(userId: UUID) {
        if (!hasInternalAuthor(userId)) {
            _authors.add(PublicationAuthor.InternalAuthor(userId))
        }
    }

    /**
     * Garantiza que el usuario figure como autor interno. Si ya está (en cualquier
     * posición) no hace nada; si no, lo inserta al principio. Permite que el orden de
     * los autores —incluido el creador— lo decida quien crea la publicación.
     */
    fun prependInternalAuthorIfAbsent(userId: UUID) {
        if (!hasInternalAuthor(userId)) {
            _authors.add(0, PublicationAuthor.InternalAuthor(userId))
        }
    }

    private fun hasInternalAuthor(userId: UUID) =
        _authors.any { it is PublicationAuthor.InternalAuthor && it.userId == userId }

    /** Añade un co-autor externo (no registrado). Se permiten nombres repetidos. */
    fun addExternalAuthor(firstName: String, lastName: String) {
        _authors.add(PublicationAuthor.ExternalAuthor(firstName, lastName))
    }
}
