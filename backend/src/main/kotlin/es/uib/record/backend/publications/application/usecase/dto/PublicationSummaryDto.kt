package es.uib.record.backend.publications.application.usecase.dto

import es.uib.record.backend.publications.domain.model.PublicationStatus
import java.time.Instant
import java.util.UUID

data class PublicationSummaryDto(
    val id: UUID,
    val title: String,
    val journalId: UUID,
    val journalName: String?,
    val groupId: UUID,
    val status: PublicationStatus,
    val createdAt: Instant,
)
