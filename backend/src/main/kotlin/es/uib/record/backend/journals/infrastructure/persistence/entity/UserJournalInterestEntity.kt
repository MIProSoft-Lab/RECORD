package es.uib.record.backend.journals.infrastructure.persistence.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "user_interest_journals")
class UserJournalInterestEntity(
    @Id @GeneratedValue(strategy = GenerationType.UUID) var id: UUID? = null,
    @Column(nullable = false, name = "user_id") var userId: UUID,
    @Column(nullable = false, name = "journal_id") var journalId: UUID,
    @Column(nullable = false, name = "created_at", updatable = false)
    var createdAt: Instant = Instant.now(),
)
