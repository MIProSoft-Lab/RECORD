package es.uib.record.backend.publications.infrastructure.persistence.entity

import es.uib.record.backend.publications.domain.model.PublicationStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

/**
 * Entrada del historial de estados de una publicación. [position] preserva el orden cronológico de
 * las transiciones.
 */
@Entity
@Table(name = "publication_status_history")
class PublicationStatusHistoryEntity(
    @Id @GeneratedValue(strategy = GenerationType.UUID) var id: UUID? = null,
    @Enumerated(EnumType.STRING) @Column(nullable = false) var status: PublicationStatus,
    @Column(name = "journal_id", nullable = false) var journalId: UUID,
    @Column(name = "changed_at", nullable = false) var changedAt: Instant,
    @Column(name = "comment", columnDefinition = "text") var comment: String? = null,
    @Column(name = "position", nullable = false) var position: Int = 0,
)
