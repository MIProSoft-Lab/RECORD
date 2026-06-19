package es.uib.record.backend.journals.domain.model

import java.time.Instant
import java.util.UUID

data class SyncState(
    val id: UUID,
    val clarivateLastUpdated: String? = null,
    val status: SyncStatus = SyncStatus.IDLE,
    val runStartedAt: Instant? = null,
    val runFinishedAt: Instant? = null,
    val processedCount: Int = 0,
    val totalCount: Int? = null,
    val failedCount: Int = 0,
)
