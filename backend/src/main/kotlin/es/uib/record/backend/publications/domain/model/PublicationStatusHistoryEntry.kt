package es.uib.record.backend.publications.domain.model

import java.time.Instant
import java.util.UUID

/**
 * Entrada del historial de estados de una publicación. Registra el momento en que la publicación
 * pasó a [status], el journal asociado en ese momento ([journalId] —útil para reflejar el journal
 * anterior tras un reenvío—) y un [comment] opcional.
 */
data class PublicationStatusHistoryEntry(
    val status: PublicationStatus,
    val journalId: UUID,
    val changedAt: Instant,
    val comment: String? = null,
)
