package es.uib.record.backend.journals.infrastructure.persistence.entity

import es.uib.record.backend.journals.domain.model.SyncStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "journal_sync_state")
class JournalSyncStateEntity(
    @Id var id: UUID,
    @Column(name = "clarivate_last_updated") var clarivateLastUpdated: String? = null,
    @Enumerated(EnumType.STRING) @Column(nullable = false) var status: SyncStatus = SyncStatus.IDLE,
    @Column(name = "run_started_at") var runStartedAt: Instant? = null,
    @Column(name = "run_finished_at") var runFinishedAt: Instant? = null,
    @Column(nullable = false, name = "processed_count") var processedCount: Int = 0,
    @Column(name = "total_count") var totalCount: Int? = null,
    @Column(nullable = false, name = "failed_count") var failedCount: Int = 0,
)
