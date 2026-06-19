package es.uib.record.backend.journals.infrastructure.mapper

import es.uib.record.backend.journals.domain.model.SyncState
import es.uib.record.backend.model.JournalSyncStatusResponse
import java.time.ZoneOffset

fun SyncState.toResponse() =
    JournalSyncStatusResponse(
        status = JournalSyncStatusResponse.Status.valueOf(this.status.name),
        processedCount = this.processedCount,
        failedCount = this.failedCount,
        totalCount = this.totalCount,
        clarivateLastUpdated = this.clarivateLastUpdated,
        runStartedAt = this.runStartedAt?.atOffset(ZoneOffset.UTC),
        runFinishedAt = this.runFinishedAt?.atOffset(ZoneOffset.UTC),
    )
