package es.uib.record.backend.publications.application.usecase.dto

import es.uib.record.backend.publications.domain.model.PublicationStatus
import java.time.Instant
import java.util.UUID

data class PublicationStatusHistoryDto(
    val status: PublicationStatus,
    val journalId: UUID,
    val journalName: String?,
    val changedAt: Instant,
    val comment: String?,
)
