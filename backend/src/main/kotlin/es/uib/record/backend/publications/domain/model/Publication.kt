package es.uib.record.backend.publications.domain.model

import es.uib.record.backend.publications.domain.exception.DoiNotAllowedException
import es.uib.record.backend.publications.domain.exception.InvalidPublicationStatusTransitionException
import es.uib.record.backend.publications.domain.exception.SameJournalResubmitException
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
    statusHistory: List<PublicationStatusHistoryEntry> = emptyList(),
) {
    private val _authors: MutableList<PublicationAuthor> = authors.toMutableList()

    val authors: List<PublicationAuthor>
        get() = _authors.toList()

    private val _statusHistory: MutableList<PublicationStatusHistoryEntry> =
        statusHistory.toMutableList()

    /** Historial de estados en orden cronológico ascendente. */
    val statusHistory: List<PublicationStatusHistoryEntry>
        get() = _statusHistory.toList()

    init {
        // Invariante: el DOI solo puede tener valor cuando la publicación está PUBLISHED.
        if (!doi.isNullOrBlank() && status != PublicationStatus.PUBLISHED) {
            throw DoiNotAllowedException()
        }
        // Siembra la primera entrada del historial para publicaciones nuevas (aún sin
        // historial persistido). Al rehidratar desde persistencia se recibe el historial
        // existente y no se vuelve a sembrar.
        if (_statusHistory.isEmpty()) {
            _statusHistory.add(PublicationStatusHistoryEntry(status, journalId, createdAt))
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

    /**
     * Devuelve una copia de la publicación con el nuevo estado [newStatus], conservando
     * el resto de campos y los autores. Solo se permiten transiciones válidas del ciclo
     * de vida; en caso contrario lanza [InvalidPublicationStatusTransitionException].
     */
    fun changeStatus(newStatus: PublicationStatus, comment: String? = null): Publication {
        if (!status.canTransitionTo(newStatus)) {
            throw InvalidPublicationStatusTransitionException(status, newStatus)
        }
        return Publication(
            id = id,
            title = title,
            abstractText = abstractText,
            doi = doi,
            journalId = journalId,
            groupId = groupId,
            status = newStatus,
            createdBy = createdBy,
            createdAt = createdAt,
            authors = authors,
            statusHistory =
                statusHistory + PublicationStatusHistoryEntry(newStatus, journalId, Instant.now(), comment),
        )
    }

    /**
     * Reenvía una publicación rechazada a otro journal [newJournalId], devolviendo una
     * copia con el nuevo journal y estado SUBMITTED. Solo se permite cuando la publicación
     * está en estado REJECTED (en caso contrario lanza
     * [InvalidPublicationStatusTransitionException]) y el journal destino debe ser distinto
     * del actual (en caso contrario lanza [SameJournalResubmitException]). Es una operación
     * atómica: cambia journal y estado a la vez, deliberadamente fuera de la máquina de
     * estados genérica de [changeStatus] para no exponer un reenvío sin cambio de journal.
     */
    fun resubmit(newJournalId: UUID, comment: String? = null): Publication {
        if (status != PublicationStatus.REJECTED) {
            throw InvalidPublicationStatusTransitionException(status, PublicationStatus.SUBMITTED)
        }
        if (newJournalId == journalId) {
            throw SameJournalResubmitException(newJournalId)
        }
        return Publication(
            id = id,
            title = title,
            abstractText = abstractText,
            doi = doi,
            journalId = newJournalId,
            groupId = groupId,
            status = PublicationStatus.SUBMITTED,
            createdBy = createdBy,
            createdAt = createdAt,
            authors = authors,
            statusHistory =
                statusHistory +
                    PublicationStatusHistoryEntry(
                        PublicationStatus.SUBMITTED,
                        newJournalId,
                        Instant.now(),
                        comment,
                    ),
        )
    }
}
