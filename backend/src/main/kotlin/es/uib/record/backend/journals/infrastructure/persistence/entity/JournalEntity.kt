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
@Table(name = "journals")
class JournalEntity(
    @Id @GeneratedValue(strategy = GenerationType.UUID) var id: UUID? = null,
    @Column(nullable = false, unique = true, name = "clarivate_id") var clarivateId: String,
    @Column(nullable = false) var name: String,
    var issn: String? = null,
    @Column(name = "e_issn") var eIssn: String? = null,
    @Column(name = "publisher_name") var publisherName: String? = null,
    @Column(name = "publisher_country") var publisherCountry: String? = null,
    @Column(nullable = false, name = "created_at", updatable = false)
    var createdAt: Instant = Instant.now(),
    @Column(name = "updated_at") var updatedAt: Instant? = null,
    @Column(name = "last_synced_at") var lastSyncedAt: Instant? = null,
)
